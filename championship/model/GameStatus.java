package com.it.br.gameserver.model.entity.event.championship.model;

public enum GameStatus {

    WIN(1),
    LOSE(2);

    int status;

    GameStatus(int status){
        this.status = status;
    }

    public int getStatusCode() {
        return status;
    }

    public void setStatusCode(int status) {
        this.status = status;
    }
}
