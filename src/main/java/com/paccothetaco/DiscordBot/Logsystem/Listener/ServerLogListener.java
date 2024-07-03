package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class ServerLogListener extends ListenerAdapter {

    private final DataManager dataManager;

    public ServerLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onEmojiAdded(@NotNull EmojiAddedEvent event) {
        event.getGuild().retrieveAuditLogs().type(ActionType.EMOJI_CREATE).limit(1).queue(entries -> {
            if (!entries.isEmpty()) {
                AuditLogEntry entry = entries.get(0);
                event.getGuild().retrieveMemberById(entry.getUserId()).queue(member -> {
                    String memberName = (member != null) ? member.getAsMention() : "Unknown";

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Emoji added")
                            .setDescription(String.format("Added Emoji: %s\nAdded by: %s", event.getEmoji().getAsMention(), memberName))
                            .setColor(Color.BLUE);

                    logServerActivity(event.getGuild(), embed);
                });
            }
        });
    }

    @Override
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        event.getGuild().retrieveAuditLogs().type(ActionType.EMOJI_DELETE).limit(1).queue(entries -> {
            if (!entries.isEmpty()) {
                AuditLogEntry entry = entries.get(0);
                event.getGuild().retrieveMemberById(entry.getUserId()).queue(member -> {
                    String memberName = (member != null) ? member.getAsMention() : "Unknown";

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Emoji removed")
                            .setDescription(String.format("Removed Emoji: %s\nRemoved by: %s", event.getEmoji().getAsMention(), memberName))
                            .setColor(Color.BLUE);

                    logServerActivity(event.getGuild(), embed);
                });
            }
        });
    }

    @Override
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Boost count updated")
                .setDescription(String.format("Boost count: %d Boosts", event.getNewBoostCount()))
                .setColor(Color.BLUE);

        logServerActivity(event.getGuild(), embed);
    }

    private void logServerActivity(Guild guild, EmbedBuilder embed) {
        if (dataManager.isServerLogActive(guild.getId())) {
            String logChannelId = dataManager.getMessageLogChannel(guild.getId());
            if (logChannelId != null) {
                TextChannel logChannel = guild.getTextChannelById(logChannelId);
                if (logChannel != null) {
                    logChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        }
    }
}
