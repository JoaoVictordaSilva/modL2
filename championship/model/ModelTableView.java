package com.it.br.gameserver.model.entity.event.championship.model;

public class ModelTableView {

    private String leaderTeam;

    private String teamName;

    public ModelTableView(){

    }

    public ModelTableView(String leaderTeam, String teamName) {
        this.leaderTeam = leaderTeam;
        this.teamName = teamName;
    }

    public String getLeaderTeam() {
        return leaderTeam;
    }

    public void setLeaderTeam(String leaderTeam) {
        this.leaderTeam = leaderTeam;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
