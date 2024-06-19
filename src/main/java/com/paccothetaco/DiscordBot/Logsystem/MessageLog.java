package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MessageLog {
    private static final Map<String, String> messageContents = new HashMap<>();
    private static final Map<String, String> messageAuthors = new HashMap<>();

    public static void onMessageReceived(MessageReceivedEvent event) {
        messageContents.put(event.getMessageId(), event.getMessage().getContentRaw());
        messageAuthors.put(event.getMessageId(), event.getAuthor().getAsMention());
    }

    public static void logMessageEdited(TextChannel logChannel, String author, String before, String after) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW)
                .setTitle("Message Edited")
                .addField("Author", author, false)
                .addField("Before", before, false)
                .addField("After", after, false);

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    public static void logMessageDeleted(TextChannel logChannel, String author, String content) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED)
                .setTitle("Message Deleted")
                .addField("Author", author, false)
                .addField("Message", content, false);

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    public static String getBeforeContent(String messageId) {
        return messageContents.getOrDefault(messageId, "Unknown Content");
    }

    public static String getAuthor(String messageId) {
        return messageAuthors.getOrDefault(messageId, "Unknown Author");
    }

    public static String getContent(String messageId) {
        return messageContents.getOrDefault(messageId, "Unknown Content");
    }
}
