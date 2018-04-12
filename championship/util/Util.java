package com.it.br.gameserver.model.entity.event.championship.util;

import com.it.br.gameserver.Announcements;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.Duel;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.network.serverpackets.ExShowScreenMessage;
import com.it.br.gameserver.network.serverpackets.StopMove;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TEAM_DEFEATED_ENEMY;

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
                if (timeToBattle.get() == 0) {
                    setEffectsToParticipate(playersToBattle, false);
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

    public static void setEffectsToParticipate(List<L2PcInstance> players, boolean paralyzed) {
        setEffectsToParticipate(players, paralyzed, true);
    }

    public static void setEffectsToParticipate(List<L2PcInstance> players, boolean paralyzed, boolean isFightingInChampionship) {
        players.forEach(it -> {
            it.setFightingInChampionship(isFightingInChampionship);
            it.sendPacket(new StopMove(it));
            it.setIsParalyzed(paralyzed);
            if (it.isStunned())
                it.setIsStunned(false);
            if (it.isImobilised())
                it.setIsImobilised(false);
            if (it.isMounted()) {
                it.dismount();
            }
            if (it.isInDuel()) {
                it.setDuelState(Duel.DUELSTATE_INTERRUPTED);
            }
        });
    }
}