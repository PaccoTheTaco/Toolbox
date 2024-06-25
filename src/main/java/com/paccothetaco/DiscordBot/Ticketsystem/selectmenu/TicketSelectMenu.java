package com.paccothetaco.DiscordBot.Ticketsystem.selectmenu;

import com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.option.ApplicationOption;
import com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.option.ReportOption;
import com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.option.SupportOption;
import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class TicketSelectMenu {

    public static void execute(StringSelectInteractionEvent event, DataManager dataManager) {
        switch (event.getValues().get(0).toLowerCase()) {
            case "support" -> SupportOption.execute(event, dataManager);
            case "report" -> ReportOption.execute(event, dataManager);
            case "application" -> ApplicationOption.execute(event, dataManager);
            default -> event.reply("This select menu option does not exist").setEphemeral(true).queue();
        }
    }
}
