package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
}
