package com.paccothetaco.DiscordBot.Utils;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

public class SelectMenuUtil {
    public static StringSelectMenu ticketSelect(List<String> activeOptions) {
        StringSelectMenu.Builder menu = StringSelectMenu.create("ticket:select")
                .setPlaceholder("Choose an option");

        if (activeOptions.contains("support")) {
            menu.addOption("Support", "support", "Hilfe und Unterstützung");
        }
        if (activeOptions.contains("report")) {
            menu.addOption("Report", "report", "Einen Benutzer melden");
        }
        if (activeOptions.contains("application")) {
            menu.addOption("Application", "application", "Bewerbung");
        }

        return menu.build();
    }
}
