package com.it.br.gameserver.model.entity.event.championship.game;

import com.it.br.gameserver.model.Olympiad.OlympiadStadia;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.game.repository.ChampionshipRepository;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipState;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants;
import com.it.br.gameserver.model.entity.event.championship.util.Util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam.TeamState.WAITING_FIGHT;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TIME_ZONE;


public class ChampionshipGameTask implements Runnable {


    private final ChampionshipTeam mTeamA;
    private final ChampionshipTeam mTeamB;
    final private ChampionshipEvent mEvent;

    ChampionshipGameTask(ChampionshipTeam teamA, ChampionshipTeam teamB) {
        this.mTeamA = teamA;
        this.mTeamB = teamB;
        this.mEvent = ChampionshipEvent.LAZY_HOLDER.getInstance();
    }

    @Override
    public void run() {
        int totalOldTotalPvpKillTeamA = mTeamA.getTotalPvpKills();
        int totalOldTotalPvpKillTeamB = mTeamB.getTotalPvpKills();

        Timestamp timestamp = new Timestamp(Calendar.getInstance(TIME_ZONE).getTimeInMillis());

        setTitleByTeamName(mTeamA, mTeamB);

        List<L2PcInstance> playersToBattle = new ArrayList<>();
        playersToBattle.addAll(mTeamA.getPlayersList());
        playersToBattle.addAll(mTeamB.getPlayersList());

        ChampionshipTeleporter teleporter = ChampionshipTeleporter.LAZY_HOLDER.getInstance();
        teleporter.init(playersToBattle);

        Util.setEffectsToParticipate(playersToBattle, true, true);

        for (OlympiadStadia STADIUM : STADIUMS) {
            if (STADIUM.isFreeToUse()) {

                STADIUM.setStadiaBusy();

                teleporter.teleportTeams(mTeamA, mTeamB, STADIUM.getCoordinates());

                Util.sendMessageToBattleBegin(playersToBattle);

                while (!mTeamA.isAllDead() && !mTeamB.isAllDead()) {
                    Util.sleepThread(ChampionshipConstants.THIRTY_SECONDS);
                }


                int totalKillsInEventTeamA = mTeamA.getTotalPvpKills() - totalOldTotalPvpKillTeamA;
                int totalDeathsInEventTeamA = mTeamB.getTotalPvpKills() - totalOldTotalPvpKillTeamB;
                ChampionshipRepository.insertChampionshipGame(mTeamA, mTeamB, totalKillsInEventTeamA, totalDeathsInEventTeamA, timestamp);

                Util.setEffectsToParticipate(playersToBattle, false, false);
                teleporter.teleportPlayerToLastPosition(playersToBattle);
                STADIUM.setStadiaFree();
                mEvent.setTeamState(WAITING_FIGHT, mTeamA, mTeamB);
                break;
            }
        }
        if (mEvent.getChampionshipState().equals(ChampionshipState.FINAL_PHASE)) {
            if (mTeamA.isAllDead()) {
                mEvent.getTeamsToBattle().remove(mTeamA);
            } else {
                mEvent.getTeamsToBattle().remove(mTeamB);
            }
        } else {
            mTeamA.increaseBattleToTeam(mTeamB);
            mEvent.setFinishedFightByTotalBattles(mTeamA, mTeamB);
        }

        synchronized (this) {
            notify();
        }


    }


    private static final OlympiadStadia[] STADIUMS =
            {
                    new OlympiadStadia(-20814, -21189, -3030),
                    new OlympiadStadia(-120324, -225077, -3331),
                    new OlympiadStadia(-120156, -207378, -3331),
                    new OlympiadStadia(-87628, -225021, -3331),
                    new OlympiadStadia(-81705, -213209, -3331),
                    new OlympiadStadia(-87593, -207339, -3331),
                    new OlympiadStadia(-93709, -218304, -3331),
                    new OlympiadStadia(-77157, -218608, -3331),
                    new OlympiadStadia(-69682, -209027, -3331),
                    new OlympiadStadia(-76887, -201256, -3331),
                    new OlympiadStadia(-109985, -218701, -3331),
                    new OlympiadStadia(-126367, -218228, -3331),
                    new OlympiadStadia(-109629, -201292, -3331),
                    new OlympiadStadia(-87523, -240169, -3331),
                    new OlympiadStadia(-81748, -245950, -3331),
                    new OlympiadStadia(-77123, -251473, -3331),
                    new OlympiadStadia(-69778, -241801, -3331),
                    new OlympiadStadia(-76754, -234014, -3331),
                    new OlympiadStadia(-93742, -251032, -3331),
                    new OlympiadStadia(-87466, -257752, -3331),
                    new OlympiadStadia(-114413, -213241, -3331)
            };

    private void setTitleByTeamName(ChampionshipTeam... teams) {
        for (ChampionshipTeam team : teams) {
            team.setTitlesByTeamName();
        }
    }
}
