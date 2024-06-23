package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketEmbedCommand;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketOptionCommand;
import com.paccothetaco.DiscordBot.Website.Website;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class CommandUtil extends ListenerAdapter {
    private final DataManager dataManager;
    private final TicketEmbedCommand ticketEmbedCommand;
    private final TicketOptionCommand ticketOptionCommand;

    public CommandUtil(DataManager dataManager) {
        this.dataManager = dataManager;
        this.ticketEmbedCommand = new TicketEmbedCommand(dataManager);
        this.ticketOptionCommand = new TicketOptionCommand(dataManager);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        switch (event.getName()) {
            case "settings" -> {
                if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
                    String sessionKey = generateSessionKey(guildId);
                    String settingsUrl = "https://paccothetaco.com/settings?sk=" + sessionKey;
                    Website.addSessionKey(sessionKey, guildId);
                    event.reply("Go to your settings: " + settingsUrl).setEphemeral(true).queue();
                } else {
                    event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
                }
            }
            case "verify" -> {
                if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
                    String sessionKey = event.getOption("sessionkey").getAsString();
                    String sessionGuildId = Website.getGuildId(sessionKey);
                    if (guildId.equals(sessionGuildId)) {
                        Website.addVerifiedSessionKey(sessionKey, guildId);
                        // Verwenden Sie eine Schaltfläche für die Weiterleitung zu den Einstellungen
                        event.reply("You have been verified. Redirecting to settings...")
                                .setEphemeral(true)
                                .addActionRow(Button.link("https://paccothetaco.com/settings?sk=" + sessionKey, "Go to Settings"))
                                .queue();
                    } else {
                        event.reply("Invalid session key for this server.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
                }
            }
            default -> event.reply("This command doesn't exist").setEphemeral(true).queue();
        }
    }

    private String generateSessionKey(String guildId) {
        SecureRandom random = new SecureRandom();
        StringBuilder sessionKey = new StringBuilder(guildId + "_");

        for (int i = 0; i < 10; i++) {
            int digit = random.nextInt(10);
            sessionKey.append(digit);
        }

        return sessionKey.toString();
    }
}
