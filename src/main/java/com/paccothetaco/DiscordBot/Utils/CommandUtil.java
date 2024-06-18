package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Logsystem.MessageLog;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketEmbedCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandUtil extends ListenerAdapter {
    private final DataManager dataManager;
    private final MessageLog messageLog;

    public CommandUtil(DataManager dataManager, MessageLog messageLog) {
        this.dataManager = dataManager;
        this.messageLog = messageLog;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        switch (event.getName()) {
            case "setwelcomechannel" -> {
                String channelId = event.getOption("channel").getAsString();
                dataManager.setWelcomeChannel(guildId, channelId);
                event.reply("Welcome channel set to <#" + channelId + ">").queue();
            }
            case "setleavechannel" -> {
                String channelId = event.getOption("channel").getAsString();
                dataManager.setLeaveChannel(guildId, channelId);
                event.reply("Leave channel set to <#" + channelId + ">").queue();
            }
            case "deactivatewelcome" -> {
                dataManager.setWelcomeActive(guildId, false);
                event.reply("Welcome messages deactivated for this server.").queue();
            }
            case "deactivateleave" -> {
                dataManager.setLeaveActive(guildId, false);
                event.reply("Leave messages deactivated for this server.").queue();
            }
            case "messagelogchannel" -> {
                String channelId = event.getOption("channel").getAsString();
                messageLog.setLogChannel(guildId, channelId);
                event.reply("Message log channel set to <#" + channelId + ">").queue();
            }
            case "deactivatemessagelog" -> {
                messageLog.deactivateLog(guildId);
                event.reply("Message logging deactivated for this server.").queue();
            }
            case "ticketembed" -> TicketEmbedCommand.execute(event);
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
            default -> event.reply("This command doesn't exist").setEphemeral(true).queue();
        }
    }
}
