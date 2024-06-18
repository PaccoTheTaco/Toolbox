package com.paccothetaco.DiscordBot.Utils;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class SelectMenuUtil {

    public static StringSelectMenu ticketSelect() {
        return StringSelectMenu.create("ticketselect")
                .addOption("Support", "support")
                .addOption("Report", "report")
                .addOption("Application", "application")
                .build();
    }
}
