package com.paccothetaco.DiscordBot.Logsystem.Listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MessageLogListener extends ListenerAdapter {
    private static final Map<String, String> messageContents = new HashMap<>();
    private static final Map<String, String> messageAuthors = new HashMap<>();
    private final DataManager dataManager;

    public MessageLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            messageContents.put(event.getMessageId(), event.getMessage().getContentRaw());
            messageAuthors.put(event.getMessageId(), event.getAuthor().getAsMention());
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        String guildId = event.getGuild().getId();
        String logChannelId = dataManager.getMessageLogChannel(guildId);
        if (logChannelId == null) return;

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW)
                .setTitle("Message Edited")
                .addField("Author", event.getAuthor().getAsMention(), false)
                .addField("Before", getBeforeContent(event.getMessageId()), false)
                .addField("After", event.getMessage().getContentDisplay(), false);
        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        String logChannelId = dataManager.getMessageLogChannel(guildId);
        if (logChannelId == null) return;

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW)
                .setTitle("Message Deleted")
                .addField("Author", getAuthor(event.getMessageId()), false)
                .addField("Message", getContent(event.getMessageId()), false);

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    private String getBeforeContent(String messageId) {
        return messageContents.getOrDefault(messageId, "Unknown Content");
    }

    private String getAuthor(String messageId) {
        return messageAuthors.getOrDefault(messageId, "Unknown Author");
    }

    private String getContent(String messageId) {
        return messageContents.getOrDefault(messageId, "Unknown Content");
    }
}
