package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;

public class MessageLog {
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
}
