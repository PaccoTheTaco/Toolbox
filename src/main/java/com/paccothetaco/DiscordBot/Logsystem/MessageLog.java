package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageLog {
    private final Map<String, String> logChannels = new HashMap<>();
    private final Map<String, Boolean> loggingActive = new HashMap<>();
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();

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
                    String oldMessage = messageCache.get(event.getMessageId());
                    String authorMention = "<@" + event.getAuthor().getId() + ">";

                    if (oldMessage == null) {
                        oldMessage = "unknown (cache may have missed it)";
                    }
                    String newMessage = event.getMessage().getContentRaw();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.ORANGE);
                    embed.setTitle("Message edited in #" + event.getChannel().getName());
                    embed.addField("Author:", authorMention, false);
                    embed.addField("Before:", oldMessage, false);
                    embed.addField("After:", newMessage, false);
                    embed.setFooter("ID: " + event.getMessageId(), null);
                    embed.setTimestamp(event.getMessage().getTimeEdited());

                    logChannel.sendMessageEmbeds(embed.build()).queue();

                    messageCache.put(event.getMessageId(), newMessage); // Update the cache with the new message content
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
                    String messageContent = messageCache.get(event.getMessageId());
                    String authorMention = "<@" + messageCache.get(event.getMessageId() + "_author") + ">";

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.RED);
                    embed.setTitle("Message deleted in #" + event.getChannel().getName());
                    embed.addField("Author:", authorMention, false);
                    embed.setDescription(messageContent != null ? messageContent : "unknown (cache may have missed it)");
                    embed.setFooter("ID: " + event.getMessageId(), null);

                    logChannel.sendMessageEmbeds(embed.build()).queue();

                    messageCache.remove(event.getMessageId());
                    messageCache.remove(event.getMessageId() + "_author");
                }
            }
        }
    }

    public void cacheMessage(String messageId, String content, String authorId) {
        messageCache.put(messageId, content);
        messageCache.put(messageId + "_author", authorId);
    }


}
