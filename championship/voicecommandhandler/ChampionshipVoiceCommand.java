package com.it.br.gameserver.model.entity.event.championship.voicecommandhandler;

import com.it.br.gameserver.cache.HtmCache;
import com.it.br.gameserver.handler.IVoicedCommandHandler;
import com.it.br.gameserver.model.actor.instance.L2PcInstance;
import com.it.br.gameserver.model.entity.event.championship.game.repository.ChampionshipRepository;
import com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants;
import com.it.br.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Calendar;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.TIME_ZONE;

public class ChampionshipVoiceCommand implements IVoicedCommandHandler {

    private static final String[] COMMAND_LIST = {"register", "unregister", "info"};

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        NpcHtmlMessage championshipEvent = new NpcHtmlMessage(5);
        if (command.equalsIgnoreCase("info")) {
            String path = "";
            championshipEvent.setHtml(getHtmForce(path));
            activeChar.sendPacket(championshipEvent);
        } else if (command.startsWith("register")) {
            if (ChampionshipConstants.DAY_OF_WEEK == Calendar.getInstance(TIME_ZONE).get(Calendar.DAY_OF_WEEK)) {
                activeChar.sendMessage(ChampionshipConstants.NOT_ALLOWED_REGISTER_IN_DAY_OF_EVENT);
            } else {
                String teamName = command.substring(9);
                ChampionshipRepository.register(activeChar, teamName);
            }

        } else if (command.startsWith("unregister")) {
            ChampionshipRepository.unregister(activeChar);
        } else if (command.startsWith("page")) {
            String page = command.substring(5);
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
        int totalPages = ChampionshipRepository.countTeam() / 10;
        StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260>");
        ChampionshipRepository.getTeamsWithPagination(page).forEach(it -> replyMSG.append("<tr><td>").append(it.getLeaderTeam()).append(it.getTeamName()).append("</td></tr>"));
        replyMSG.append("<tr>");
        for (int i = 0; i < totalPages; i++) {
            if (i == page)
                replyMSG.append("<a action=\"").append(i + 1);
            else
                replyMSG.append("<a action=\"bypass -h championship_page ").append(i + 1);
            replyMSG.append("\"><font color=\"LEVEL\">");
            replyMSG.append(i + 1).append("</font></a>");
        }
        replyMSG.append("</tr>");
        replyMSG.append("</table>");
        return replyMSG.toString();
    }

    private String getHtmForce(String path) {
        return HtmCache.getInstance().getHtmForce(path);
    }

}
