package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.util.HashMap;
import java.util.Map;

public class MessageLog {
    private final Map<String, String> logChannels = new HashMap<>();
    private final Map<String, Boolean> loggingActive = new HashMap<>();

    public void setLogChannel(String guildId, String channelId) {
        logChannels.put(guildId, channelId);
        loggingActive.put(guildId, true);
    }

    public void deactivateLog(String guildId) {
        loggingActive.put(guildId, false);
    }

    public void handleMessageUpdate(MessageUpdateEvent event) {
        String guildId = event.getGuild().getId();
        if (Boolean.TRUE.equals(loggingActive.get(guildId))) {
            String channelId = logChannels.get(guildId);
            if (channelId != null) {
                TextChannel logChannel = event.getGuild().getTextChannelById(channelId);
                if (logChannel != null) {
                    logChannel.sendMessage("Message edited by " + event.getAuthor().getAsTag() +
                            " in <#" + event.getChannel().getId() + ">: \n" +
                            "Old message: " + event.getMessage().getContentRaw()).queue();
                }
            }
        }
    }

    public void handleMessageDelete(MessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        if (Boolean.TRUE.equals(loggingActive.get(guildId))) {
            String channelId = logChannels.get(guildId);
            if (channelId != null) {
                TextChannel logChannel = event.getGuild().getTextChannelById(channelId);
                if (logChannel != null) {
                    logChannel.sendMessage("Message deleted in <#" + event.getChannel().getId() + ">.").queue();
                }
            }
        }
    }
}
