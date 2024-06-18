package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages.ChannelDataManager;
import com.paccothetaco.DiscordBot.Logsystem.MessageLog;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandUtil extends ListenerAdapter {
    private final ChannelDataManager channelDataManager;
    private final MessageLog messageLog;

    public CommandUtil(ChannelDataManager channelDataManager, MessageLog messageLog) {
        this.channelDataManager = channelDataManager;
        this.messageLog = messageLog;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        if (event.getName().equals("setwelcomechannel")) {
            String channelId = event.getOption("channel").getAsString();
            channelDataManager.setWelcomeChannel(guildId, channelId);
            event.reply("Welcome channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("setleavechannel")) {
            String channelId = event.getOption("channel").getAsString();
            channelDataManager.setLeaveChannel(guildId, channelId);
            event.reply("Leave channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("deactivatewelcome")) {
            channelDataManager.setWelcomeActive(guildId, false);
            event.reply("Welcome messages deactivated for this server.").queue();
        } else if (event.getName().equals("deactivateleave")) {
            channelDataManager.setLeaveActive(guildId, false);
            event.reply("Leave messages deactivated for this server.").queue();
        } else if (event.getName().equals("messagelogchannel")) {
            String channelId = event.getOption("channel").getAsString();
            messageLog.setLogChannel(guildId, channelId);
            event.reply("Message log channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("deactivatemessagelog")) {
            messageLog.deactivateLog(guildId);
            event.reply("Message logging deactivated for this server.").queue();
        }
    }
}
