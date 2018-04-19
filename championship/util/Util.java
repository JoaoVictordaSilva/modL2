package com.it.br.gameserver.model.entity.event.championship.util;

import com.it.br.gameserver.Announcements;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.Duel;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class Util {


    public static void sendMessageToBattleBegin(List<L2PcInstance> playersToBattle) {
        AtomicInteger timeToBattle = new AtomicInteger(10);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                playersToBattle.forEach(player ->
                        player.sendPacket(
                                new ExShowScreenMessage("The battle will start in " + timeToBattle.get() + " seconds", 1000, 2, true)));
                timeToBattle.getAndDecrement();
                if (timeToBattle.get() <= 0) {
                    setEffectsToParticipate(playersToBattle);
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }

    public static void sendMessageWinnerOfBattle(ChampionshipTeam teamA, ChampionshipTeam teamB) {
        if (teamA.isAllDead())
            Announcements.getInstance().announceToAll(String.format(TEAM_DEFEATED_ENEMY, teamB.getTeamName(), teamA.getTeamName()));
        else
            Announcements.getInstance().announceToAll(String.format(TEAM_DEFEATED_ENEMY, teamA.getTeamName(), teamB.getTeamName()));
    }

    public static void sendMessageToAll(String message, Object... args) {
        Announcements.getInstance().announceToAll(String.format(message, args));
    }

    public static void sleepThread(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitThis(Thread thread) {
        synchronized (thread) {
            try {
                thread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setEffectsToParticipate(List<L2PcInstance> players) {
        setEffectsToParticipate(players, true, true);
    }

    public static void setEffectsToParticipate(List<L2PcInstance> players, boolean canAttack, boolean isFightingInChampionship) {
        players.forEach(it -> {
            it.setFightingInChampionship(isFightingInChampionship);
            it.setCanAttack(canAttack);
            it.setCurrentCp(it.getMaxCp());
            it.setCurrentHpMp(it.getMaxHp(), it.getMaxMp());

            if (it.isStunned())
                it.setIsStunned(false);

            if (it.isImobilised())
                it.setIsImobilised(false);

            if (it.isMounted())
                it.dismount();

            if (it.isInDuel())
                it.setDuelState(Duel.DUELSTATE_INTERRUPTED);

            if (it.getPvpFlag() > 0)
                it.setPvpFlag(0);

            if (it.getKarma() > 0)
                it.setKarma(0);

        });
    }

    public static boolean isLeaderPartyAndNotInCommandChannel(L2PcInstance player) {
        if (!player.isInParty()) {
            player.sendMessage(NEED_PARTY_AND_BE_LEADER_PARTY_TO_PARTICIPATE);
            return false;
        }
        if (!player.getParty().getPartyMembers().get(0).equals(player)) {
            player.sendMessage(NEED_BE_LEADER_PARTY);
            return false;
        }

        if (player.getParty().isInCommandChannel()) {
            player.sendMessage(CANT_BE_IN_COMMAND_CHANNEL);
            return false;
        }

        return true;
    }

    public static void announce(long time) {
        time = time / 1000;
        if (time >= 3600) {
            System.out.println("Championship Event: " + (time / 60 / 60) + " hour(s) until event start!");
        } else if (time >= 60) {
            System.out.println("Championship Event: " + (time / 60) + " minute(s) until event start!");
        } else {
            System.out.println("Championship Event: " + time + " second(s) until event start!");
        }
    }

}