package com.it.br.gameserver.model.entity.event.championship.model;

public class ChampionshipGame {

    private int id;
    private int kill;
    private GameStatus gameStatus;
    private int death;
    private int resurrection;
    private int teamId;

    public ChampionshipGame() {
    }

    public ChampionshipGame(int kill, GameStatus gameStatus, int death, int resurrection, int teamId) {
        this.kill = kill;
        this.gameStatus = gameStatus;
        this.death = death;
        this.resurrection = resurrection;
        this.teamId = teamId;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getDeath() {
        return death;
    }

    public void setDeath(int death) {
        this.death = death;
    }

    public int getResurrection() {
        return resurrection;
    }

    public void setResurrection(int resurrection) {
        this.resurrection = resurrection;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
