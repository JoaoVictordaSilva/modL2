package com.it.br.gameserver.model.entity.event.championship.voicecommandhandler;

import com.it.br.gameserver.cache.HtmCache;
import com.it.br.gameserver.handler.IVoicedCommandHandler;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.game.repository.ChampionshipRepository;
import com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants;
import com.it.br.gameserver.network.serverpackets.NpcHtmlMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.INSERT_VALID_TEAM_NAME;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TEAM_NAME_TOO_LONG;
import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TIME_ZONE;

public class ChampionshipVoiceCommand implements IVoicedCommandHandler {

    private static final String[] COMMAND_LIST = {"championship_register", "championship_page", "unregister", "info"};
    private static ChampionshipVoiceCommand INSTANCE;

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        NpcHtmlMessage championshipEvent = new NpcHtmlMessage(0);
        if (command.equalsIgnoreCase("info")) {
            championshipEvent.setHtml(getHtmForce());
            activeChar.sendPacket(championshipEvent);
        } else if (command.startsWith("championship_register")) {
            if (ChampionshipConstants.DAY_OF_WEEK == Calendar.getInstance(TIME_ZONE).get(Calendar.DAY_OF_WEEK)) {
                activeChar.sendMessage(ChampionshipConstants.NOT_ALLOWED_REGISTER_IN_DAY_OF_EVENT);
            } else {
                String teamName = command.substring(22);
                if ("".equals(teamName)) {
                    activeChar.sendMessage(INSERT_VALID_TEAM_NAME);
                    return false;
                }
                if (teamName.length() > 16) {
                    activeChar.sendMessage(TEAM_NAME_TOO_LONG);
                    return false;
                }
                ChampionshipRepository.register(activeChar, teamName);
            }

        } else if (command.startsWith("unregister")) {
            ChampionshipRepository.unregister(activeChar);
        } else if (command.startsWith("championship_page")) {
            String page = command.length() > 17 ? command.substring(17) : "";
            if (page.equals(""))
                page = "0";
            championshipEvent.setHtml(showTeamsRegisteredWithPagination(Integer.parseInt(page)));
            activeChar.sendPacket(championshipEvent);
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return COMMAND_LIST;
    }

    private String showTeamsRegisteredWithPagination(int page) {

        double totalPages = (double) ChampionshipRepository.countTeam() / (double) 20;

        if (totalPages % 1 != 0)
            totalPages = (int) totalPages + 1;

        StringBuilder replyMSG = new StringBuilder("<html><body>")
                .append("<center><font color=\"LEVEL\">Championship Event</font></center><br>")
                .append("<center>Teams registered until now</center><br><center><table width=150>");

        ChampionshipRepository.getTeamsWithPagination(page * 20)
                .forEach(it -> replyMSG.append("<tr><td>")
                        .append(it.getLeaderTeam()).append("</td>")
                        .append("<td>").append(it.getTeamName()).append("</td></tr>")
                        .append("<tr></tr>"));

        replyMSG.append("</table></center><center><table width=100><tr>");

        for (int i = 0; i < totalPages && totalPages < 9; i++) {
            replyMSG.append("<td width=10>");

            if (i == page)
                replyMSG.append(i + 1);
            else
                replyMSG.append("<a action=\"bypass -h championship_page").append(i).append("\">").append(i + 1).append("</a>")
                        .append("</td>");
        }

        replyMSG.append("</tr></table></center>");

        return replyMSG.toString();
    }

    private String getHtmForce() {
        return HtmCache.getInstance().getHtmForce("\\data\\html\\championship\\championship_info.htm");
    }

    public static class LAZY_HOLDER {
        public static ChampionshipVoiceCommand getInstance() {
            if (INSTANCE == null)
                INSTANCE = new ChampionshipVoiceCommand();

            return INSTANCE;
        }
    }

}
