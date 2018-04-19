package com.it.br.gameserver.model.entity.event.championship.game;

import com.it.br.gameserver.Announcements;
import com.it.br.gameserver.datatables.sql.ItemTable;
import com.it.br.gameserver.model.PcInventory;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.game.repository.ChampionshipRepository;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipState;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.model.entity.event.championship.schedule.ChampionshipEventSchedule;
import com.it.br.gameserver.model.entity.event.championship.util.Util;
import com.it.br.gameserver.network.SystemMessageId;
import com.it.br.gameserver.network.serverpackets.SystemMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam.TeamState.*;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.THERE_ARE_NOT_ENOUGH_REGISTERED_TEAM;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.THERE_ARE_NOT_REGISTERED_TEAM;

public class ChampionshipEvent implements Runnable {

    public enum EventState {
        INACTIVE, WAITING_UNTIL_STARTED, STARTED, FINISHED
    }

    private static final Logger LOGGER = Logger.getLogger(ChampionshipEvent.class.getName());
    private static ChampionshipEvent INSTANCE;
    private static List<ChampionshipTeam> TEAMS_TO_BATTLE = Collections.synchronizedList(new ArrayList<>());
    private static ChampionshipState STATE;
    private static List<Integer[]> REWARDS_ITEM_ID = new ArrayList<>(Arrays.<Integer[]>asList(new Integer[]{57, 1000000}));
    private static EventState EVENT_STATE = EventState.INACTIVE;

    private Random mRandom = new Random();
    private List<ChampionshipTeam> mTeamsRegistered;
    private List<ChampionshipTeam> mAuxList;
    private List<Integer> mLosersTeamById;

    private ChampionshipEvent() {
    }

    public void start() {
        LOGGER.info("Starting engine([ChampionshipEvent])");
        ChampionshipEventSchedule.LAZY_HOLDER.schedule();
    }

    @Override
    public void run() {
        if (checkTeamsRegistered())
            prepareBattle();
    }

    private void prepareBattle() {
        setState(EventState.STARTED);
        insertTeams();
        startBattle();
    }

    private void startBattle() {
        STATE = ChampionshipState.BATTLE_CLASSIFICATION;
        startGames();
        startLoserSeries();
    }

    private void startFinalPhase() {
        startGames();
        ChampionshipTeam championshipTeam = TEAMS_TO_BATTLE.get(0);
        ChampionshipRepository.insertChampion(championshipTeam.getId());
        rewardTeam(championshipTeam);
    }

    private void rewardTeam(ChampionshipTeam team) {
        team.getPlayersList().forEach(it -> {
            PcInventory inv = it.getInventory();
            REWARDS_ITEM_ID.forEach(itemId -> giveItemsAndSendMessage(inv, itemId));
        });

    }

    private void startGames() {
        if (STATE.equals(ChampionshipState.BATTLE_CLASSIFICATION) || STATE.equals(ChampionshipState.LOSER_SERIES)) {
            startGameToBattleClassificationOrLoserSeries();
        } else {
            startGameToFinalPhase();
        }
    }

    private void startGameToBattleClassificationOrLoserSeries() {
        Thread thread = null;
        while (!finishedFights()) {
            waitWhenOnlyOneWaitingOrAllFighting(thread);
            ChampionshipTeam[] championshipTeams = sortTeams(STATE);
            thread = getTeamsSortedAndStartGameTask(championshipTeams, thread);
        }
    }

    private void startGameToFinalPhase() {
        Thread thread = null;
        while (TEAMS_TO_BATTLE.size() != 1) {
            waitWhenOnlyOneWaitingOrAllFighting(thread);
            ChampionshipTeam[] championshipTeams = sortTeamsFinalPhase();
            thread = getTeamsSortedAndStartGameTask(championshipTeams, thread);
        }
    }

    private Thread getTeamsSortedAndStartGameTask(ChampionshipTeam[] championshipTeams, Thread thread) {
        if (championshipTeams[0] != null && championshipTeams[1] != null) {
            System.out.println(championshipTeams[0].getTeamName() + " versus " + championshipTeams[1].getTeamName());
            thread = new Thread(new ChampionshipGameTask(championshipTeams[0], championshipTeams[1]));
            setTeamState(FIGHTING, championshipTeams[0], championshipTeams[1]);
            thread.start();
        }
        return thread;
    }

    private void waitWhenOnlyOneWaitingOrAllFighting(Thread thread) {
        if (thread == null) return;
        if (hasOnlyOneWaiting()) {
            Util.waitThis(thread);
            LOGGER.info("One team waiting fight");
            System.out.println("ONLY ONE");
        } else if (allFighting()) {
            Util.waitThis(thread);
            LOGGER.info("All teams fighting");
            System.out.println("ALL FIGHTING");
        }
    }

    private void startLoserSeries() {
        STATE = ChampionshipState.LOSER_SERIES;
        clearInfoTeams();
        mLosersTeamById = ChampionshipRepository.getLosersTeamById();
        if (mLosersTeamById != null && mLosersTeamById.size() == 1) {
            LOGGER.info("******Initializing FINAL PHASE******");
            TEAMS_TO_BATTLE.removeIf(it -> it.getId() == mLosersTeamById.get(0));
            STATE = ChampionshipState.FINAL_PHASE;
            startFinalPhase();
        } else {
            LOGGER.info("******Initializing LOSER SERIES******");
            List<ChampionshipTeam> teamsWaitingLoserSeriesEnd = TEAMS_TO_BATTLE.stream().map(it -> {
                if (mLosersTeamById.stream().noneMatch(id -> it.getId() == id))
                    return it;
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            TEAMS_TO_BATTLE.removeAll(teamsWaitingLoserSeriesEnd);
            startGames();
            TEAMS_TO_BATTLE.addAll(teamsWaitingLoserSeriesEnd);
            startLoserSeries();
        }
    }

    private void clearInfoTeams() {
        TEAMS_TO_BATTLE.forEach(it -> {
            it.getMapTeamFaces().clear();
            it.clearTotalBattles();
            it.setTeamState(WAITING_FIGHT);
        });
    }

    private ChampionshipTeam[] sortTeams(ChampionshipState state) {
        ChampionshipTeam team;
        ChampionshipTeam[] championshipTeams = new ChampionshipTeam[2];
        while ((team = getRandomTeam()) != null) {
            if (team.getTeamState().equals(FINISHED_FIGHTS)) {
                mAuxList.remove(team);
                continue;
            }

            if (team.getTeamState().equals(WAITING_FIGHT)) {
                if (championshipTeams[0] == null || championshipTeams[0].getTeamState().equals(FINISHED_FIGHTS)) {
                    championshipTeams[0] = team;
                    mAuxList.remove(team);
                    continue;
                }
                if (championshipTeams[1] == null) {
                    if (ChampionshipState.BATTLE_CLASSIFICATION.equals(state)) {
                        if (!championshipTeams[0].foughtTwoTimesSameTeam(team.getId())) {
                            championshipTeams[1] = team;
                            mAuxList.remove(team);
                            break;
                        }
                    } else {
                        if (!championshipTeams[0].foughtSameTeam(team.getId())) {
                            championshipTeams[1] = team;
                            mAuxList.remove(team);
                            break;
                        }
                    }
                }
            }
        }
        mAuxList = TEAMS_TO_BATTLE.stream().filter(it -> !it.getTeamState().equals(FINISHED_FIGHTS)).collect(Collectors.toList());
        return championshipTeams;
    }

    private ChampionshipTeam[] sortTeamsFinalPhase() {
        ChampionshipTeam team;
        ChampionshipTeam[] championshipTeams = new ChampionshipTeam[2];
        mAuxList = new ArrayList<>(TEAMS_TO_BATTLE);
        while ((team = getRandomTeam()) != null) {
            if (team.getTeamState().equals(WAITING_FIGHT) && championshipTeams[0] == null) {
                championshipTeams[0] = team;
                mAuxList.remove(team);
                continue;
            }
            if (team.getTeamState().equals(WAITING_FIGHT) && championshipTeams[1] == null) {
                championshipTeams[1] = team;
                mAuxList.remove(team);
                break;
            }

        }
        return championshipTeams;
    }

    public void setFinishedFightByTotalBattles(ChampionshipTeam... teams) {
        for (ChampionshipTeam team : teams) {
            if (ChampionshipState.BATTLE_CLASSIFICATION.equals(STATE)) {
                if (team.getTotalBattles() == (TEAMS_TO_BATTLE.size() - 1) * 2)
                    team.setTeamState(FINISHED_FIGHTS);
            } else if (ChampionshipState.LOSER_SERIES.equals(STATE)) {
                if (team.getTotalBattles() == TEAMS_TO_BATTLE.size() - 1)
                    team.setTeamState(FINISHED_FIGHTS);
            }
        }
    }

    private boolean hasOnlyOneWaiting() {
        return TEAMS_TO_BATTLE.stream().filter(it -> it.getTeamState().equals(WAITING_FIGHT)).collect(Collectors.toList()).size() == 1;
    }

    private boolean allFighting() {
        return TEAMS_TO_BATTLE.stream().allMatch(it -> it.getTeamState().equals(FIGHTING));
    }

    private boolean finishedFights() {
        return TEAMS_TO_BATTLE.stream().allMatch(it -> it.getTeamState().equals(FINISHED_FIGHTS));
    }

    private ChampionshipTeam getRandomTeam() {
        if (mAuxList.size() == 0)
            return null;
        return mAuxList.get(mRandom.nextInt(mAuxList.size()));
    }

    private void addTeam(ChampionshipTeam championshipTeam) {
        if (championshipTeam == null || championshipTeam.getLeader() == null)
            return;

        if (!Util.isLeaderPartyAndNotInCommandChannel(championshipTeam.getLeader())) {
            return;
        }

        TEAMS_TO_BATTLE.add(championshipTeam);

    }

    private void insertTeams() {
        mTeamsRegistered.forEach(this::addTeam);
        mAuxList = new ArrayList<>(TEAMS_TO_BATTLE);
    }

    private boolean checkTeamsRegistered() {
        mTeamsRegistered = ChampionshipRepository.getTeamsRegistered();
        return hasTeamsRegistered();
    }

    public void insertTeamByAdm(Collection<L2PcInstance> allPlayer) {
        mTeamsRegistered = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(allPlayer.size());
        allPlayer.forEach(l2PcInstance -> {
            mTeamsRegistered.add(new ChampionshipTeam(l2PcInstance.getObjectId(), l2PcInstance.getParty().getPartyMembers(), "Team " + i));
            i.getAndIncrement();
        });
    }


    private List<L2PcInstance> getLeaderList() {
        return mTeamsRegistered.stream().map(ChampionshipTeam::getLeader).collect(Collectors.toList());
    }

    private void giveItemsAndSendMessage(PcInventory inv, Integer[] itemId) {
        SystemMessage systemMessage;
        L2PcInstance player = inv.getOwner();
        if (ItemTable.getInstance().createDummyItem(itemId[0]).isStackable()) {
            inv.addItem("Championship Event", itemId[0], itemId[1], player, player);
            if (itemId[1] > 1) {
                systemMessage = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
                systemMessage.addItemName(itemId[0]);
                systemMessage.addNumber(itemId[1]);
            } else {
                systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
                systemMessage.addItemName(itemId[0]);
            }
            player.sendPacket(systemMessage);
        } else {
            for (int i = 0; i < itemId[1]; ++i) {
                inv.addItem("Championship Event", itemId[0], 1, player, player);
                systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
                systemMessage.addItemName(itemId[0]);
                player.sendPacket(systemMessage);
            }
        }
    }

    public boolean hasTeamsRegistered() {
//        if (mTeamsRegistered != null && mTeamsRegistered.size() == 0) {
//            LOGGER.info(THERE_ARE_NOT_REGISTERED_TEAM);
//            Announcements.getInstance().gameAnnounceToAll(THERE_ARE_NOT_REGISTERED_TEAM);
//            return false;
//        }
//        if (mTeamsRegistered.size() < 3) {
//            LOGGER.info(THERE_ARE_NOT_ENOUGH_REGISTERED_TEAM);
//            Announcements.getInstance().gameAnnounceToAll(THERE_ARE_NOT_ENOUGH_REGISTERED_TEAM);
//            return false;
//        }
        return true;
    }

    public EventState getState() {
        return EVENT_STATE;
    }

    public void setState(EventState state) {
        EVENT_STATE = state;
    }

    public void setTeamState(ChampionshipTeam.TeamState teamState, ChampionshipTeam... teams) {
        for (ChampionshipTeam team : teams) {
            team.setTeamState(teamState);
        }
    }

    public ChampionshipState getChampionshipState() {
        return STATE;
    }


    public List<ChampionshipTeam> getTeamsToBattle() {
        return TEAMS_TO_BATTLE;
    }

    public static void onLogout(L2PcInstance player) {
        if (EVENT_STATE.equals(EventState.STARTED))
            TEAMS_TO_BATTLE.stream()
                    .map(ChampionshipTeam::getPlayersList)
                    .filter(it -> (it.contains(player)))
                    .findFirst()
                    .ifPresent(it -> {
                        it.remove(player);
                        ChampionshipTeleporter.LAZY_HOLDER.getInstance().teleportPlayerToLastPosition(player);
                    });

    }

    public static class LAZY_HOLDER {

        public static ChampionshipEvent getInstance() {
            if (INSTANCE == null)
                INSTANCE = new ChampionshipEvent();
            return INSTANCE;
        }

    }
}
