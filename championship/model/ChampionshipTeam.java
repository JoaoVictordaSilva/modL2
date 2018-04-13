package com.it.br.gameserver.model.entity.event.championship.model;

import com.it.br.gameserver.model.actor.instance.L2PcInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChampionshipTeam {

    private int id;
    private List<L2PcInstance> playersList;
    private String teamName;
    private TeamState teamState = TeamState.WAITING_FIGHT;
    private int totalPvpKill;
    private Map<Integer, Integer> mapTeamFaces;
    private int totalBattles = 0;

    public enum TeamState {
        WAITING_FIGHT, FINISHED_FIGHTS, FIGHTING
    }

    public ChampionshipTeam(int id, List<L2PcInstance> playersList, String teamName) {
        this.id = id;
        this.playersList = playersList;
        this.teamName = teamName;
        mapTeamFaces = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<L2PcInstance> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<L2PcInstance> playersList) {
        this.playersList = playersList;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public L2PcInstance getLeader() {
        return playersList.stream().filter(ob -> ob.getParty().isLeader(ob)).findFirst().get();
    }

    public TeamState getTeamState() {
        return teamState;
    }

    public void setTeamState(TeamState teamState) {
        this.teamState = teamState;
    }

    public boolean isAllDead() {
        return playersList.stream().filter(L2PcInstance::isDead).collect(Collectors.toList()).size() == playersList.size();
    }

    public int getTotalPvpKills() {
        for (L2PcInstance player : playersList) {
            totalPvpKill = +player.getPvpKills();
        }
        return totalPvpKill;
    }

    public void setTotalPvpKill(int totalPvpKill) {
        this.totalPvpKill = totalPvpKill;
    }

    public void clearTotalPvpKill() {
        this.totalPvpKill = 0;
    }

    public Map<Integer, Integer> getMapTeamFaces() {
        return mapTeamFaces;
    }

    public void setMapTeamFaces(Map<Integer, Integer> mapTeamFaces) {
        this.mapTeamFaces = mapTeamFaces;
    }

    public synchronized void increaseBattleToTeam(ChampionshipTeam team) {
        Integer battlesFirstTeam = mapTeamFaces.get(team.getId());
        if (battlesFirstTeam == null)
            battlesFirstTeam = 0;

        battlesFirstTeam++;
        increaseBattles();
        mapTeamFaces.put(team.getId(), battlesFirstTeam);

        Integer battlesSecondTeam = team.getMapTeamFaces().get(id);
        if (battlesSecondTeam == null)
            battlesSecondTeam = 0;

        battlesSecondTeam++;

        team.increaseBattles();
        team.getMapTeamFaces().put(id, battlesSecondTeam);
    }

    public boolean foughtSameTeam(int teamId) {
        return mapTeamFaces.get(teamId) != null && mapTeamFaces.get(teamId) == 1;
    }

    public synchronized boolean foughtTwoTimesSameTeam(int teamId) {
        return mapTeamFaces.get(teamId) != null && mapTeamFaces.get(teamId) == 2;
    }

    public int getTotalBattles() {
        return totalBattles;
    }

    public void setTotalBattles(int totalBattles) {
        this.totalBattles = totalBattles;
    }

    public void clearTotalBattles() {
        totalBattles = 0;
    }

    private synchronized void increaseBattles() {
        totalBattles++;
    }

}
