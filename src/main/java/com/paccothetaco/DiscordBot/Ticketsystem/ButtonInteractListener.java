package com.paccothetaco.DiscordBot.Ticketsystem;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Ticketsystem.button.CloseTicketButton;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class ButtonInteractListener extends ListenerAdapter {
    private final DataManager dataManager;

    public ButtonInteractListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("close-ticket")) {
            CloseTicketButton.execute(event, dataManager);
        }
    }
}
