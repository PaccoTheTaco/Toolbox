package com.paccothetaco.DiscordBot.Ticketsystem.command;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class TicketOptionCommand extends ListenerAdapter {
    private final DataManager dataManager;
    private final TicketEmbedCommand ticketEmbedCommand;

    public TicketOptionCommand(DataManager dataManager) {
        this.dataManager = dataManager;
        this.ticketEmbedCommand = new TicketEmbedCommand(dataManager);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ticketoption")) return;

        String guildId = event.getGuild().getId();
        String action = event.getOption("action").getAsString();
        String option = event.getOption("option").getAsString();

        boolean activate = action.equalsIgnoreCase("activate");
        dataManager.setTicketOption(guildId, option, activate);

        event.reply("Option " + option + " has been " + (activate ? "activated" : "deactivated") + ".").setEphemeral(true).queue();

        ticketEmbedCommand.sendNewTicketEmbed(event, true);
    }
}
