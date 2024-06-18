package com.paccothetaco.DiscordBot.Ticketsystem;

import com.paccothetaco.DiscordBot.Ticketsystem.selectmenu.TicketSelectMenu;
import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SelectMenuInteractListener extends ListenerAdapter {
    private final DataManager dataManager;

    public SelectMenuInteractListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        TicketSelectMenu.execute(event, dataManager);
    }
}
