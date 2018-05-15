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
  CONSTRAINT pk_id_game PRIMARY KEY(id_game),
  CONSTRAINT fk_game_id_team FOREIGN KEY(id_team) REFERENCES team(id_team)
);

CREATE TABLE IF NOT EXISTS champion(
  id_champion INTEGER AUTO_INCREMENT,
  victory_date DATETIME NOT NULL,
  id_team INTEGER,
  CONSTRAINT pk_id_champion PRIMARY KEY(id_champion),
  CONSTRAINT fk_champion_id_team FOREIGN KEY(id_team) REFERENCES team(id_team)
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
CONSTRAINT fk_id_game_versus FOREIGN KEY(id_game_versus) REFERENCES game_versus(id_game_versus)
);

--Lottery
CREATE TABLE IF NOT EXISTS lottery_bid(
id INTEGER AUTO_INCREMENT,
id_item INTEGER NOT NULL,
quantity INTEGER NOT NULL,
id_player INTEGER NOT NULL,
id_team INTEGER NOT NULL,
active BIT DEFAULT FALSE,
CONSTRAINT pk_id_lottery PRIMARY KEY(id),
CONSTRAINT fk_lottery_bid_id_item FOREIGN KEY(id_item) REFERENCES etcitem(item_id)--TODO verify the references on that table
CONSTRAINT fk_lottery_bid_id_player FOREIGN KEY(id_player) REFERENCES characters(obj_Id)--TODO verify the references on that table
);

CREATE TABLE IF NOT EXISTS lottery_bid_history(
id INTEGER AUTO_INCREMENT,
id_lottery_bid INTEGER NOT NULL,
time_bid DATETIME,
CONSTRAINT pk_id_lottery_bid_history PRIMARY KEY(id),
CONSTRAINT fk_lottery_bid_history_id_lottery_bid FOREIGN KEY(id_lottery_bid) REFERENCES lottery_bid(id)
);

CREATE TABLE IF NOT EXISTS lottery_winner(
id INTEGER AUTO_INCREMENT
id_lottery_bid INTEGER NOT NULL,
CONSTRAINT pk_id_lottery_winner PRIMARY KEY(id),
CONSTRAINT fk_lottery_winner_id_lottery_bid FOREIGN KEY(id_lottery_bid) REFERENCES lottery_bid(id)
);










