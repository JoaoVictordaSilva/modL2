package com.it.br.gameserver.model.entity.event.championship.game;

import com.it.br.gameserver.model.Location;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.model.entity.event.championship.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TEN_SECONDS;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.YOU_WILL_BE_TELEPORTED;

public class ChampionshipTeleporter {

    private static ChampionshipTeleporter INSTANCE;
    private Map<L2PcInstance, Integer[]> mPlayerLocationMap = new HashMap<>();

    private ChampionshipTeleporter() {
    }

    public void init(List<L2PcInstance> players) {
        oldLocationPlayer(players);
    }

    public void teleportTeams(ChampionshipTeam teamA, ChampionshipTeam teamB, int[] location) {
        sendMessageAndTeleport(teamA.getPlayersList(), location, 900);
        sendMessageAndTeleport(teamB.getPlayersList(), location, -900);
        Util.sleepThread(TEN_SECONDS);
    }

    private void oldLocationPlayer(List<L2PcInstance> players) {
        players.forEach(player -> mPlayerLocationMap.put(player, new Integer[]{player.getX(), player.getY(), player.getZ()}));
    }

    public void teleportPlayerToLastPosition(List<L2PcInstance> players) {
        teleportPlayers(players);
    }

    private void teleportPlayers(List<L2PcInstance> players) {
        players.forEach(it -> {
            it.sendMessage(YOU_WILL_BE_TELEPORTED);
            it.teleToLocation(new Location(mPlayerLocationMap.get(it)), true);
            clearPlayerLocationMap(it);
        });
    }

    public void teleportPlayerToLastPosition(L2PcInstance player) {
        if (mPlayerLocationMap.get(player) != null) {
            player.teleToLocation(new Location(mPlayerLocationMap.get(player)), true);
            clearPlayerLocationMap(player);
        }
    }

    public Map<L2PcInstance, Integer[]> getPlayerLocationMap() {
        return mPlayerLocationMap;
    }

    private void clearPlayerLocationMap(L2PcInstance obj) {
        mPlayerLocationMap.remove(obj);
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
