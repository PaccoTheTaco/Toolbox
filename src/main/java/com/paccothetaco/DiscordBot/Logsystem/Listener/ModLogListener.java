package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.BiConsumer;

public class ModLogListener extends ListenerAdapter {
    private final DataManager dataManager;

    public ModLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getUser();
                getModeratorAndReason(event.getGuild(), ActionType.BAN, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, "banned", moderator, reason);
                });
            }
        }
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getUser();
                getModeratorAndReason(event.getGuild(), ActionType.UNBAN, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, "unbanned", moderator, reason);
                });
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getUser();
                getModeratorAndReason(event.getGuild(), ActionType.KICK, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, "kicked", moderator, reason);
                });
            }
        }
    }

    @Override
    public void onGuildVoiceGuildMute(@NotNull GuildVoiceGuildMuteEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                String action = event.isGuildMuted() ? "muted" : "unmuted";
                getModeratorAndReason(event.getGuild(), ActionType.MEMBER_UPDATE, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, action, moderator, reason);
                });
            }
        }
    }

    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                String action = event.isGuildDeafened() ? "deafen" : "undeafen";
                getModeratorAndReason(event.getGuild(), ActionType.MEMBER_UPDATE, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, action, moderator, reason);
                });
            }
        }
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
        if (dataManager.isModLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                User targetUser = event.getMember().getUser();
                String action = event.getNewTimeOutEnd() != null ? "timed out" : "timeout removed";
                getModeratorAndReason(event.getGuild(), ActionType.MEMBER_UPDATE, targetUser, (moderator, reason) -> {
                    logModAction(logChannel, targetUser, action, moderator, reason);
                });
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

    private void logModAction(TextChannel logChannel, User targetUser, String action, User moderator, String reason) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);

        switch (action) {
            case "muted":
            case "unmuted":
            case "deafen":
            case "undeafen":
            case "timed out":
            case "timeout removed":
            case "kicked":
            case "banned":
            case "unbanned":
                embed.setTitle("Member " + action);
                embed.setDescription("Member: " + targetUser.getAsMention() + "\n" +
                        action + " by: " + (moderator != null ? moderator.getAsMention() : "unknown") + "\n" +
                        "Reason: " + (reason != null ? reason : "none"));
                break;
            default:
                embed.setTitle("Moderation Log");
                embed.setDescription("Member @" + targetUser.getAsTag() + " was " + action);
        }

        embed.setThumbnail(targetUser.getAvatarUrl());
        embed.setTimestamp(java.time.Instant.now());

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    private void getModeratorAndReason(Guild guild, ActionType actionType, User targetUser, BiConsumer<User, String> callback) {
        guild.retrieveAuditLogs().type(actionType).limit(10).queue(auditLogEntries -> {
            for (AuditLogEntry entry : auditLogEntries) {
                if (entry.getTargetId().equals(targetUser.getId())) {
                    User moderator = entry.getUser();
                    String reason = entry.getReason();
                    callback.accept(moderator, reason);
                    return;
                }
            }
            // Fallback if no audit log entry found
            callback.accept(null, "none");
        });
    }
}
