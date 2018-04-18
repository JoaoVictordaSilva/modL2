package com.it.br.gameserver.model.entity.event.championship.game.repository;

import com.it.br.L2DatabaseFactory;
import com.it.br.gameserver.model.L2World;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipGame;
import com.it.br.gameserver.model.entity.event.championship.model.ChampionshipTeam;
import com.it.br.gameserver.model.entity.event.championship.model.GameStatus;
import com.it.br.gameserver.model.entity.event.championship.model.ModelTableView;
import com.it.br.gameserver.model.entity.event.championship.util.Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class ChampionshipRepository {

    public static final String SELECT_TEAM = "SELECT * FROM team WHERE active = TRUE";
    private static final String SELECT_TEAM_WHERE_ID_TEAM_LEADER = "SELECT * FROM team WHERE id_team_leader = ? AND active = TRUE";
    private static final String INSERT_INTO_TEAM = "INSERT INTO team(id_team_leader, team_name, active) VALUES(?,?,?)";
    private static final String DELETE_FROM_TEAM_WHERE_ID_TEAM_LEADER = "DELETE FROM team WHERE id_team_leader = ?";
    private static final String UPDATE_FROM_TEAM_WHERE_ID_TEAM_LEADER = "UPDATE team SET active = FALSE WHERE id_team_leader = ?";
    private static final String INSERT_INTO_GAME = "INSERT INTO game (kill_, status_game, death, resurrection, id_team) VALUES(?,?,?,?,?)";

    public static final String[] FIELD_NAME_TEAM = {"id_team", "id_team_leader", "team_name"};
    public static final String[] FIELD_NAME_GAME = {"id_game", "kill_", "status_game", "death", "resurrection", "id_team"};

    private static final String SELECT_COUNT_WIN_WHERE_ID_TEAM = "SELECT count(status_game) from game where status_game = 1 and id_team = ?";
    private static final String SELECT_COUNT_LOSE_WHERE_ID_TEAM = "SELECT count(status_game) from game where status_game = 2 and id_team = ?";
    private static final String TRUNCATE_TEAM = "TRUNCATE TABLE team";
    //Retorna 1(VERDADEIRO) para o id_team que for igual ao numero maximo de derrota
    private static final String SELECT_ID_TEAM_MAX_LOSE =
            "SELECT id_team, count(status_game) = (SELECT MAX(status_game) FROM ( SELECT id_team, count(status_game) AS status_game FROM game " +
                    "WHERE status_game = 2  GROUP BY id_team ) AS MAX_LOSE)  AS MAXIMO_DERROTA_1 FROM game  WHERE status_game = 2 " +
                    "GROUP BY id_team HAVING MAXIMO_DERROTA_1 = 1";

    private static final String FIND_TEAM_NAME = "SELECT name FROM team WHERE name = ? AND active = TRUE";
    private static final String SELECT_TEAM_TABLE_VIEW_PAGINATION = "SELECT c.char_name, t.team_name FROM team t JOIN characters c ON t.id_team_leader = c.obj_Id LIMIT ?,20";
    private static final String COUNT_TEAM = "SELECT COUNT(*) FROM team";
    private static final String SELECT_FROM_CHAMPION = "SELECT t.id_team, t.id_team_leader, t.team_name FROM champion c JOIN team t ON c.id_team = t.id_team";
    public static final String SELECT_CHAMPION_TABLE_VIEW = "SELECT ch.char_name, t.team_name, DATE_FORMAT(victory_date,'%M/%d/%Y' %H:%i) " +
                                                                "FROM champion c " +
                                                                "JOIN team t " +
                                                                "ON c.id_team = t.id_team" +
                                                                "JOIN characters ch " +
                                                                "ON ch.obj_Id = t.id_team_leader";
    private static final String INSERT_INTO_GAME_VERSUS = "INSERT INTO game_versus(id_game_one, id_game_two) VALUES(?,?)";
    private static final String INSERT_INTO_HISTORY = "INSERT INTO history(id_game_versus, battle_time ) VALUES(?,?)";
    private static final String LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
    private static final String INSERT_INTO_CHAMPION = "INSERT INTO champion(id_team, victory_date) VALUES(?,?)";

    private static final Logger LOGGER = Logger.getLogger(ChampionshipRepository.class.getName());


    private static PreparedStatement preparedStatement(String statement) {
        try {
            return L2DatabaseFactory.getInstance().getConnection().prepareStatement(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(DB_CONNECTION_ERROR);
    }

    public static void truncateTable() {
        PreparedStatement statement = preparedStatement(TRUNCATE_TEAM);
        try {
            statement.execute();
        } catch (SQLException e) {
            LOGGER.warning("Error at truncate table");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized static int insertIntoChampionshipGameAndReturnLastInsertId(ChampionshipGame game) {
        PreparedStatement statement = preparedStatement(INSERT_INTO_GAME);
        try {
            statement.setInt(1, game.getKill());
            statement.setInt(2, game.getGameStatus().getStatusCode());
            statement.setInt(3, game.getDeath());
            statement.setInt(4, game.getResurrection());
            statement.setInt(5, game.getTeamId());
            statement.execute();
            ResultSet resultSet = statement.executeQuery(LAST_INSERT_ID);
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            LOGGER.warning("Error at insert game and return last inserted id");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    static int countWinsOrLoses(int idChampionshipTeam, GameStatus status) {
        PreparedStatement statement;
        if (status.getStatusCode() == 1)
            statement = preparedStatement(SELECT_COUNT_WIN_WHERE_ID_TEAM);
        else
            statement = preparedStatement(SELECT_COUNT_LOSE_WHERE_ID_TEAM);
        try {
            statement.setInt(1, idChampionshipTeam);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public synchronized static void insertChampionshipGame(ChampionshipTeam teamA, ChampionshipTeam teamB, int totalKillsInEventTeamA, int totalDeathsInEventTeamA, Timestamp timestamp) {
        ChampionshipGame game = new ChampionshipGame();
        game.setKill(totalKillsInEventTeamA);
        game.setDeath(totalDeathsInEventTeamA);
        game.setResurrection(teamA.getTotalResurrectionAccepted());
        game.setTeamId(teamA.getId());

        if (teamA.isAllDead())
            game.setGameStatus(GameStatus.LOSE);
        else
            game.setGameStatus(GameStatus.WIN);

        int idGameOne = insertIntoChampionshipGameAndReturnLastInsertId(game);

        game.setKill(totalDeathsInEventTeamA);
        game.setDeath(totalKillsInEventTeamA);
        game.setGameStatus(game.getGameStatus().getStatusCode() == 1 ? GameStatus.LOSE : GameStatus.WIN);
        game.setResurrection(teamB.getTotalResurrectionAccepted());
        game.setTeamId(teamB.getId());

        int idGameTwo = insertIntoChampionshipGameAndReturnLastInsertId(game);

        insertIntoGameVersusAndAuditHistory(idGameOne, idGameTwo, timestamp);
    }

    public static List<Integer> getLosersTeamById() {
        PreparedStatement preparedStatement = preparedStatement(SELECT_ID_TEAM_MAX_LOSE);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Integer> integers = new ArrayList<>();
            while (resultSet.next()) {
                integers.add(resultSet.getInt(1));
            }
            return integers;
        } catch (SQLException e) {
            LOGGER.info("Error to losers team by id");
            e.printStackTrace();
            try {
                preparedStatement.getConnection().close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    private static boolean isPlayerRegistered(L2PcInstance player) {
        PreparedStatement statement = preparedStatement(SELECT_TEAM_WHERE_ID_TEAM_LEADER);
        try {
            statement.setInt(1, player.getObjectId());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            LOGGER.warning("Error at insert game and return last inserted id");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean register(L2PcInstance player, String teamName) {
        if (player == null)
            return false;

        if (isPlayerRegistered(player)) {
            player.sendMessage(ALREADY_REGISTERED);
            return false;
        }

        if (findTeamName(teamName)) {
            player.sendMessage(TEAM_NAME_ALREADY_REGISTERED);
            return false;
        }

        PreparedStatement statement = preparedStatement(INSERT_INTO_TEAM);
        try {
            statement.setInt(1, player.getObjectId());
            statement.setString(2, teamName);
            statement.setBoolean(3, true);
            statement.execute();
            player.sendMessage(REGISTERED);
        } catch (SQLException e) {
            LOGGER.warning("Error at register");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static void unregister(L2PcInstance player) {
        PreparedStatement statement = preparedStatement(DELETE_FROM_TEAM_WHERE_ID_TEAM_LEADER);
        try {
            statement.setInt(1, player.getObjectId());
            if (statement.executeUpdate() == 1)
                player.sendMessage(UNREGISTERED);
            else
                player.sendMessage(NOT_REGISTERED);
        } catch (SQLException e) {
            LOGGER.warning("Error at unregister");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean findTeamName(String teamName) {
        PreparedStatement preparedStatement = preparedStatement(FIND_TEAM_NAME);
        try {
            preparedStatement.setString(1, teamName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            LOGGER.warning("Error at find team name");
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static List<ModelTableView> getTeamsWithPagination(int page) {
        PreparedStatement preparedStatement = preparedStatement(SELECT_TEAM_TABLE_VIEW_PAGINATION);
        List<ModelTableView> modelTableViews = new ArrayList<>();

        try {
            preparedStatement.setInt(1, page);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String charName = resultSet.getString(1);
                String teamName = resultSet.getString(2);
                modelTableViews.add(new ModelTableView(charName, teamName));
            }
        } catch (SQLException e) {
            LOGGER.warning("Error at team with pagination");
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return modelTableViews;
    }

    public static int countTeam() {
        PreparedStatement preparedStatement = preparedStatement(COUNT_TEAM);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            LOGGER.warning("Error at count teams");
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public static List<ChampionshipTeam> getChampions() {
        PreparedStatement statement = preparedStatement(SELECT_FROM_CHAMPION);
        List<ChampionshipTeam> championshipTeams = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int teamId = resultSet.getInt(FIELD_NAME_TEAM[0]);
                L2PcInstance leaderPlayer = L2World.getInstance().getPlayer(resultSet.getInt(FIELD_NAME_TEAM[1]));
                String teamName = resultSet.getString(FIELD_NAME_TEAM[2]);
                championshipTeams.add(new ChampionshipTeam(teamId, leaderPlayer.getParty().getPartyMembers(), teamName));
            }
        } catch (SQLException e) {
            LOGGER.warning("Error get champions");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return championshipTeams;
    }

    public static List<ModelTableView> getChampionsTableView() {
        PreparedStatement statement = preparedStatement(SELECT_CHAMPION_TABLE_VIEW);
        List<ModelTableView> modelTableViews = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {

                String charName = resultSet.getString(1);
                String teamName = resultSet.getString(2);
                Timestamp battleTime = resultSet.getTimestamp(3);

                modelTableViews.add(new ModelTableView(charName, teamName, battleTime));
            }
        } catch (SQLException e) {
            LOGGER.warning("Error get champions to table view");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return modelTableViews;
    }

    private static void insertIntoGameVersusAndAuditHistory(int idGameOne, int idGameTwo, Timestamp timestamp) {
        PreparedStatement statement = preparedStatement(INSERT_INTO_GAME_VERSUS);
        try {
            statement.setInt(1, idGameOne);
            statement.setInt(2, idGameTwo);
            statement.execute();
            ResultSet resultSet = statement.executeQuery(LAST_INSERT_ID);
            resultSet.next();
            int idGameVersus = resultSet.getInt(1);
            statement.close();
            auditHistory(idGameVersus, timestamp);
        } catch (SQLException e) {
            LOGGER.warning("Error at insert game versus");
            e.printStackTrace();
        }
    }

    private static void auditHistory(int idGameVersus, Timestamp timestamp) {
        PreparedStatement statement = preparedStatement(INSERT_INTO_HISTORY);
        try {
            statement.setInt(1, idGameVersus);
            statement.setTimestamp(2, timestamp);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.warning("Error at audit history");
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ChampionshipTeam> getTeamsRegistered() {
        PreparedStatement statement = preparedStatement(SELECT_TEAM);
        List<ChampionshipTeam> teamsRegistered = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int teamId = resultSet.getInt(FIELD_NAME_TEAM[0]);
                L2PcInstance leaderPlayer = L2World.getInstance().getPlayer(resultSet.getInt(FIELD_NAME_TEAM[1]));
                String teamName = resultSet.getString(FIELD_NAME_TEAM[2]);

                if (Util.isLeaderPartyAndNotInCommandChannel(leaderPlayer))
                    teamsRegistered.add(new ChampionshipTeam(teamId, leaderPlayer.getParty().getPartyMembers(), teamName));
            }

        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teamsRegistered;
    }

    public static void insertChampion(int idChampionshipTeam) {
        PreparedStatement statement = preparedStatement(INSERT_INTO_CHAMPION);

        try {
            statement.setInt(1, idChampionshipTeam);
            statement.setTimestamp(2, new Timestamp(Calendar.getInstance(TIME_ZONE).getTimeInMillis()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
