package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class ModLogListener extends ListenerAdapter {
    private final DataManager dataManager;

    public ModLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getUser();
                logModAction(logChannel, targetUser, "kicked/banned");
            }
        }
    }

    @Override
    public void onGuildVoiceGuildMute(@NotNull GuildVoiceGuildMuteEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                logModAction(logChannel, targetUser, "muted");
            }
        }
    }

    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                logModAction(logChannel, targetUser, "deafened");
            }
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                logModAction(logChannel, targetUser, "timed out");
            }
        }
    }

    private TextChannel getLogChannel(Guild guild) {
        String logChannelId = dataManager.getMessageLogChannel(guild.getId());
        if (logChannelId != null) {
            return guild.getTextChannelById(logChannelId);
        }
        return null;
    }

    private void logModAction(TextChannel logChannel, User targetUser, String action) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setTitle("Moderation Log");

        String description = String.format("Member @%s was %s", targetUser.getAsTag(), action);

        embed.setDescription(description);
        embed.setThumbnail(targetUser.getAvatarUrl());
        embed.setTimestamp(java.time.Instant.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
