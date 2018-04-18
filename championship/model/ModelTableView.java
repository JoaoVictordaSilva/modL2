package com.it.br.gameserver.model.entity.event.championship.model;

import java.util.Date;

public class ModelTableView {

    private String teamLeaderName;

    private String teamName;

    private Date battleDate;

    public ModelTableView(String teamLeaderName, String teamName) {
        this.teamLeaderName = teamLeaderName;
        this.teamName = teamName;
    }

    public ModelTableView(String teamLeaderName, String teamName, Date battleDate) {
        this.teamLeaderName = teamLeaderName;
        this.teamName = teamName;
        this.battleDate = battleDate;
    }

    public String getTeamLeaderName() {
        return teamLeaderName;
    }

    public void setTeamLeaderName(String teamLeaderName) {
        this.teamLeaderName = teamLeaderName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Date getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(Date battleDate) {
        this.battleDate = battleDate;
    }
}
