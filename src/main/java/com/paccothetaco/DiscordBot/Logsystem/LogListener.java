package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class LogListener extends ListenerAdapter {
    private final DataManager dataManager;
    private final Map<String, String> messageContents = new HashMap<>();
    private final Map<String, String> messageAuthors = new HashMap<>();

    public LogListener(DataManager dataManager) {
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

        String beforeContent = messageContents.getOrDefault(event.getMessageId(), "Unknown Content");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW)
                .setTitle("Message Edited")
                .addField("Author", event.getAuthor().getAsMention(), false)
                .addField("Before", beforeContent, false)
                .addField("After", event.getMessage().getContentDisplay(), false)
                .setFooter("Message ID: " + event.getMessageId(), null)
                .setTimestamp(OffsetDateTime.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();

        // Update the message content in the map
        messageContents.put(event.getMessageId(), event.getMessage().getContentRaw());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        String logChannelId = dataManager.getMessageLogChannel(guildId);
        if (logChannelId == null) return;

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        String author = messageAuthors.getOrDefault(event.getMessageId(), "Unknown Author");
        String content = messageContents.getOrDefault(event.getMessageId(), "Unknown Content");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED)
                .setTitle("Message Deleted")
                .addField("Author", author, false)
                .addField("Message", content, false)
                .setFooter("Message ID: " + event.getMessageId(), null)
                .setTimestamp(OffsetDateTime.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
