package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogListener extends ListenerAdapter {
    private final MessageLog messageLog;

    public LogListener(MessageLog messageLog) {
        this.messageLog = messageLog;
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (!event.getAuthor().isBot()) { // Ignore bot messages
            messageLog.handleMessageUpdate(event);
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        messageLog.handleMessageDelete(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) { // Ignore bot messages
            messageLog.cacheMessage(event.getMessageId(), event.getMessage().getContentRaw(), event.getAuthor().getId());
        }
    }
}
