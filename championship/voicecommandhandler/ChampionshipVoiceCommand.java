package com.it.br.gameserver.model.entity.event.championship.voicecommandhandler;

import com.it.br.gameserver.cache.HtmCache;
import com.it.br.gameserver.handler.IVoicedCommandHandler;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.game.repository.ChampionshipRepository;
import com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants;
import com.it.br.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Calendar;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class ChampionshipVoiceCommand implements IVoicedCommandHandler {

    private static final String[] COMMAND_LIST = {"championship_register", "championship_page", "championship_confirm",
            "championship_champion", "unregister", "info"};
    private static ChampionshipVoiceCommand INSTANCE;

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        NpcHtmlMessage championshipEvent = new NpcHtmlMessage(0);
        if (command.equalsIgnoreCase("info")) {
            championshipEvent.setHtml(getHtmForce());
            activeChar.sendPacket(championshipEvent);
        } else if (command.startsWith("unregister")) {
            ChampionshipRepository.unregister(activeChar);
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
                championshipEvent.setHtml(showConfirmTeamName(teamName));
                activeChar.sendPacket(championshipEvent);
            }

        } else if (command.startsWith("championship_confirm")) {
            String teamName = command.substring(21);
            ChampionshipRepository.register(activeChar, teamName);
        } else if (command.startsWith("championship_page")) {
            String page = command.length() > 17 ? command.substring(17) : "";
            if (page.equals(""))
                page = "0";
            championshipEvent.setHtml(showTeamsRegisteredWithPagination(Integer.parseInt(page)));
            activeChar.sendPacket(championshipEvent);
        } else if (command.startsWith("championship_champion")) {
            showChampionPage();
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return COMMAND_LIST;
    }

    private String showTeamsRegisteredWithPagination(int page) {

        double totalPages = (double) ChampionshipRepository.countTeam() / 20;

        if (totalPages % 1 != 0)
            totalPages = (int) totalPages + 1;

        StringBuilder replyMSG = new StringBuilder("<html><body>")
                .append("<center><font color=\"LEVEL\">Championship Event</font></center><br>")
                .append("<center>Teams registered until now</center><br><center><table width=150>");

        replyMSG.append("<tr><td>")
                .append("Team Leader Name</td>")
                .append("<td>Team Name</td></tr>")
                .append("<tr></tr>");

        ChampionshipRepository.getTeamsWithPagination(page * 20)
                .forEach(it -> replyMSG.append("<tr><td>")
                        .append(it.getTeamLeaderName()).append("</td>")
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

    private String showConfirmTeamName(String teamName) {
        return "<html><body>Are you sure you want to register with this team name: " + teamName + " " +
                "<button value=\"Confirm\" action=\"bypass -h championship_confirm" + teamName + "\" width=55 height=15\n" +
                "back=\"sek.cbui94\" fore=\"sek.cbui92\"></body></html>";
    }

    private void showChampionPage() {
        StringBuilder replyMSG = new StringBuilder("<html><body>")
                .append("<center><font color=\"LEVEL\">Championship Event</font></center><br>")
                .append("<center>Teams registered until now</center><br><center><table width=150>")
                .append("<tr><td>")
                .append("Leader Name</td>")
                .append("<td>Team Name</td>")
                .append("<td>Date of Victory</td></tr>")
                .append("<tr></tr>");

        ChampionshipRepository.getChampionsTableView()
                .forEach(it -> replyMSG.append("<tr><td>")
                        .append(it.getTeamLeaderName()).append("</td>")
                        .append("<td>").append(it.getTeamName()).append("</td>")
                        .append("<td>").append(it.getBattleDate()).append("</td></tr>")
                        .append("<tr></tr>"));

        replyMSG.append("</table></center><center><table width=100><tr>");
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
