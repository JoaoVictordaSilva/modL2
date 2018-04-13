package com.it.br.gameserver.model.entity.event.championship.game;

import com.it.br.gameserver.model.Location;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.model.entity.event.championship.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class ChampionshipTeleporter {

    private static ChampionshipTeleporter INSTANCE;
    private ChampionshipTeam mTeamA, mTeamB;
    private Map<L2PcInstance, Integer[]> mPlayerLocationMap = new HashMap<>();

    private ChampionshipTeleporter() {
    }

    public void init(ChampionshipTeam teamA, ChampionshipTeam teamB) {
        this.mTeamA = teamA;
        this.mTeamB = teamB;
        oldLocationPlayer(mTeamA.getPlayersList());
        oldLocationPlayer(mTeamB.getPlayersList());
    }

    public void teleportTeams(int[] location) {
        sendMessageAndTeleport(mTeamA.getPlayersList(), location, 900);
        sendMessageAndTeleport(mTeamB.getPlayersList(), location, -900);
        Util.sleepThread(TEN_SECONDS);
    }

    private void oldLocationPlayer(List<L2PcInstance> players) {
        players.forEach(player -> mPlayerLocationMap.put(player, new Integer[]{player.getX(), player.getY(), player.getZ()}));
    }

    public void teleportPlayerToLastPosition() {
        mPlayerLocationMap.keySet().forEach(player -> {
            player.sendMessage(YOU_WILL_BE_TELEPORTED);
            player.teleToLocation(new Location(mPlayerLocationMap.get(player)), true);
        });
        clearPlayerLocationMap();
    }

    public Map<L2PcInstance, Integer[]> getPlayerLocationMap() {
        return mPlayerLocationMap;
    }

    private void clearPlayerLocationMap() {
        mPlayerLocationMap.clear();
    }

    private void sendMessageAndTeleport(List<L2PcInstance> players, int[] location, int xLocation) {
        players.forEach(l2PcInstance -> {
            l2PcInstance.sendMessage(YOU_WILL_BE_TELEPORTED);
            l2PcInstance.teleToLocation(location[0] + xLocation, location[1], location[2], true);
        });
    }


    public static class LAZY_HOLDER {
        public static ChampionshipTeleporter getInstance() {
            if (INSTANCE == null)
                INSTANCE = new ChampionshipTeleporter();
            return INSTANCE;
        }
    }

}
