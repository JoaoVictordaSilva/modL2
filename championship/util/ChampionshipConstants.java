package com.it.br.gameserver.model.entity.event.championship.util;

import java.util.Calendar;
import java.util.TimeZone;

public class ChampionshipConstants {

    public static final String NEED_PARTY_AND_BE_LEADER_PARTY_TO_PARTICIPATE = "You need to create party to participate the event and be the leader party!";
    public static final String NEED_BE_LEADER_PARTY = "You need to be the leader party!";
    public static final String CANT_BE_IN_COMMAND_CHANNEL = "You can't be in command channel, please quit from channel!";
    public static final String NOT_REGISTERED = "You aren't registered";
    public static final String ALREADY_REGISTERED = "You are already registered!";
    public static final String NOT_ALLOWED_REGISTER_IN_DAY_OF_EVENT = "Doesn't allowed register in day of event!";
    public static final String UNREGISTERED = "Successfully unregistered!";
    public static final String REGISTERED = "Successfully registered!";
    public static final String YOU_WILL_BE_TELEPORTED = "You will be teleported in few seconds!";
    public static final String TEAM_NAME_ALREADY_REGISTERED = "Team name already registered, choose other!";
    public static final String DB_CONNECTION_ERROR = "Connection with database is wrong.";
    public static final String THERE_ARE_NOT_REGISTERED_TEAM = "Canceling the event. There are not registered teams";
    public static final String THERE_ARE_NOT_ENOUGH_REGISTERED_TEAM = "Canceling the event. There aren't enough registered  teams, minimum participants are 4 teams";
    public static final String THAT_THE_BEST_WIN = "Good Luck! That the best win!";
    static final String TEAM_DEFEATED_ENEMY = "%s defeated %s";
    public static final String INSERT_VALID_TEAM_NAME = "Insert a valid team name!";
    public static final String TEAM_NAME_TOO_LONG = "The team name is too long, the maximum of characters is 16!";
    private static final String TIME_ZONE_ID = "America/Sao_Paulo";
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone(TIME_ZONE_ID);
    public static final int PER_SECOND = 1000;
    private static final int FIVE_SECONDS = 5000;
    public static final int TEN_SECONDS = 10000;
    public static final int TWENTY_SECONDS = 20000;
    public static final int THIRTY_SECONDS = 30000;
    public static final int FIFTEENTH = TEN_SECONDS + FIVE_SECONDS;
    public static final int DAY_OF_WEEK = Calendar.SATURDAY;
    public static final int HOUR_OF_DAY = 22;
    public static final int MINUTE = 0;
    public static final int SECOND = 0;
    public static final int PER_WEEK = 604800000;
    public static final int PER_HOUR = 3600000;
    public static final int PER_HALF_HOUR = PER_HOUR / 2;
    public static final int PER_TEN_MINUTE = PER_HALF_HOUR / 3;
    public static final int PER_TWENTY_MINUTE = PER_TEN_MINUTE * 2;
    public static final int PER_FIVE_MINUTE = PER_TEN_MINUTE / 2;
    public static final int PER_MINUTE = PER_FIVE_MINUTE / 5;
}
