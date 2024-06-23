package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketEmbedCommand;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketOptionCommand;
import com.paccothetaco.DiscordBot.Website.Website;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
            case "ticketcategory" -> {
                String categoryId = event.getOption("category").getAsString();
                dataManager.setTicketCategory(guildId, categoryId);
                event.reply("Ticket category set to <#" + categoryId + ">").queue();
            }
            case "closedticketcategory" -> {
                String categoryId = event.getOption("category").getAsString();
                dataManager.setClosedTicketCategory(guildId, categoryId);
                event.reply("Closed ticket category set to <#" + categoryId + ">").queue();
            }
            case "setmodrole" -> {
                String roleId = event.getOption("modrole").getAsString();
                dataManager.setModRole(guildId, roleId);
                event.reply("Moderator role set to <@&" + roleId + ">").queue();
            }
            case "messagelogchannel" -> {
                String channelId = event.getOption("channel").getAsString();
                dataManager.setMessageLogChannel(guildId, channelId);
                event.reply("Message log channel set to <#" + channelId + ">").queue();
            }
            case "deactivatemessagelog" -> {
                dataManager.deactivateMessageLog(guildId);
                event.reply("Message logging deactivated for this server.").queue();
            }
            case "ticketoption" -> ticketOptionCommand.onSlashCommandInteraction(event);
            case "settings" -> {
                if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
                    String sessionKey = generateSessionKey(guildId);
                    String settingsUrl = "http://localhost:8080/settings?sk=" + sessionKey;
                    Website.addSessionKey(sessionKey, guildId);
                    event.reply("Go to your settings: " + settingsUrl).setEphemeral(true).queue();
                } else {
                    event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
                }
            }
            case "verify" -> {
                if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
                    String sessionKey = event.getOption("sessionkey").getAsString();
                    Website.addVerifiedSessionKey(sessionKey, guildId);
                    event.reply("You have been verified. Please refresh the settings page.").setEphemeral(true).queue();
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
