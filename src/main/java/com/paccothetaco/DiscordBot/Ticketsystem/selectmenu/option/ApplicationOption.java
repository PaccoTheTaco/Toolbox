package com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.option;

import com.paccothetaco.DiscordBot.Ticketsystem.TicketType;
import com.paccothetaco.DiscordBot.Utils.TicketUtil;
import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class ApplicationOption {
    public static void execute(StringSelectInteractionEvent event, DataManager dataManager) {
        TicketUtil.open(TicketType.APPLICATION, event, dataManager);
    }
}
