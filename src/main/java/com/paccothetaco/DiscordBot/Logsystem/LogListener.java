package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.paccothetaco.DiscordBot.DataManager;

public class LogListener extends ListenerAdapter {
    private final DataManager dataManager;

    public LogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            MessageLog.onMessageReceived(event);
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        String guildId = event.getGuild().getId();
        String logChannelId = dataManager.getMessageLogChannel(guildId);
        if (logChannelId == null) return;

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        MessageLog.logMessageEdited(logChannel, event.getAuthor().getAsMention(),
                MessageLog.getBeforeContent(event.getMessageId()),
                event.getMessage().getContentDisplay());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        String logChannelId = dataManager.getMessageLogChannel(guildId);
        if (logChannelId == null) return;

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        MessageLog.logMessageDeleted(logChannel,
                MessageLog.getAuthor(event.getMessageId()),
                MessageLog.getContent(event.getMessageId()));
    }
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (dataManager.isJoinLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                logChannel.sendMessage(event.getMember().getUser().getAsTag() + " has joined the server.").queue();
            }
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (dataManager.isLeaveLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                logChannel.sendMessage(event.getUser().getAsTag() + " has left the server.").queue();
            }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (dataManager.isChangeNicknameLogActive(event.getGuild().getId())) {
            TextChannel logChannel = getLogChannel(event.getGuild());
            if (logChannel != null) {
                logChannel.sendMessage(event.getMember().getUser().getAsTag() + " has changed their nickname from " +
                        event.getOldNickname() + " to " + event.getNewNickname() + ".").queue();
            }
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.isMember(event.getUser()) && dataManager.isChangeNameLogActive(guild.getId())) {
                TextChannel logChannel = getLogChannel(guild);
                if (logChannel != null) {
                    logChannel.sendMessage(event.getUser().getAsTag() + " has changed their name from " +
                            event.getOldName() + " to " + event.getNewName() + ".").queue();
                }
            }
        });
    }

    private TextChannel getLogChannel(Guild guild) {
        String logChannelId = dataManager.getMessageLogChannel(guild.getId());
        return logChannelId != null ? guild.getTextChannelById(logChannelId) : null;
    }
}

