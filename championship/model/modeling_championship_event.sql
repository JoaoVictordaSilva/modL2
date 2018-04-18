CREATE TABLE IF NOT EXISTS team(
  id_team INTEGER AUTO_INCREMENT,
  id_team_leader INTEGER,
  team_name VARCHAR(50),
  active BIT DEFAULT FALSE,
  CONSTRAINT pk_id_team PRIMARY KEY(id_team)
);

CREATE TABLE IF NOT EXISTS game(
  id_game INTEGER AUTO_INCREMENT,
  kill_ INTEGER,
  status_game INTEGER, /*1(WIN) OR 2(LOSE)*/
  death INTEGER,
  resurrection INTEGER,
  id_team INTEGER,
  CONSTRAINT PK_ID_GAME PRIMARY KEY(id_game),
  CONSTRAINT FK_ID_TEAM FOREIGN KEY(id_team) REFERENCES team(id_team)
);

CREATE TABLE IF NOT EXISTS champion(
  id_champion INTEGER AUTO_INCREMENT,
  victory_date DATETIME NOT NULL,
  id_team INTEGER,
  CONSTRAINT pk_id_champion PRIMARY KEY(id_champion),
  CONSTRAINT fk_id_team FOREIGN KEY(id_team) REFERENCES team(id_team)
);

CREATE TABLE IF NOT EXISTS championship(
id_championship INTEGER AUTO_INCREMENT,
id_champion NOT NULL,
CONSTRAINT pk_id_championship PRIMARY KEY(id_championship),
CONSTRAINT fk_id_champion FOREIGN KEY(id_champion) REFERENCES champion(id_champion)
);

CREATE TABLE game_versus(
id_game_versus INTEGER AUTO_INCREMENT,
id_game_one INTEGER NOT NULL,
id_game_two INTEGER NOT NULL,
CONSTRAINT pk_id_game_versus PRIMARY KEY(id_game_versus),
CONSTRAINT fk_id_team_game_one FOREIGN KEY(id_game_one) REFERENCES game(id_game),
CONSTRAINT fk_id_team_game_two FOREIGN KEY(id_game_two) REFERENCES game(id_game)
);

CREATE TABLE history(
id_history INTEGER AUTO_INCREMENT,
id_game_versus INTEGER NOT NULL,
battle_time DATETIME NOT NULL,
CONSTRAINT pk_id_history PRIMARY KEY(id_history),
CONSTRAINT fk_id_game_versus FOREIGN KEY(id_game_versus)
);

CREATE UNIQUE INDEX ix_id_team_leader ON team(id_team_leader);
CREATE UNIQUE INDEX ix_id_game_versus ON game_versus(id_game_versus);
CREATE UNIQUE INDEX ix_id_history ON history(id_history);
CREATE UNIQUE INDEX ix_id_game ON game(id_team)










