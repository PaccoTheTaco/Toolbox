package com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.option;

import com.paccothetaco.DiscordBot.Ticketsystem.TicketType;
import com.paccothetaco.DiscordBot.Utils.TicketUtil;
import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class ReportOption {
    public static void execute(StringSelectInteractionEvent event, DataManager dataManager) {
        TicketUtil.open(TicketType.REPORT, event, dataManager);
    }
}
