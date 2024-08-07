package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ChannelListener extends ListenerAdapter {
    private DataManager dataManager;

    public ChannelListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private static final String LOG_CHANNEL_ID = "1254398510439075910";

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (!dataManager.isChannelLogActive(event.getGuild().getId())) {
            return;
        }

        TextChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Channel Created");
        embed.addField("Channel Name", event.getChannel().getName(), false);

        String permissions = "N/A";
        if (event.getChannel() instanceof TextChannel) {
            permissions = ((TextChannel) event.getChannel()).getPermissionOverride(event.getGuild().getPublicRole()).toString();
        } else if (event.getChannel() instanceof VoiceChannel) {
            permissions = ((VoiceChannel) event.getChannel()).getPermissionOverride(event.getGuild().getPublicRole()).toString();
        } else if (event.getChannel() instanceof Category) {
            permissions = ((Category) event.getChannel()).getPermissionOverride(event.getGuild().getPublicRole()).toString();
        }

        embed.addField("Channel Permissions", permissions, false);
        embed.addField("Created By", event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getAsTag(), false);
        embed.setFooter(event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getName());
        embed.setThumbnail(event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getAvatarUrl());
        embed.setColor(Color.GREEN);
        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!dataManager.isChannelLogActive(event.getGuild().getId())) {
            return;
        }

        TextChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Channel Deleted");
        embed.addField("Channel Name", event.getChannel().getName(), false);
        embed.addField("Deleted By", event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getAsTag(), false);
        embed.setFooter(event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getName());
        embed.setThumbnail(event.getGuild().retrieveAuditLogs().complete().get(0).getUser().getAvatarUrl());
        embed.setColor(Color.RED);
        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onGenericChannelUpdate(GenericChannelUpdateEvent event) {
        if (!dataManager.isChannelLogActive(event.getGuild().getId())) {
            return;
        }

        TextChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Channel Updated");
        embed.addField("Channel Name", event.getChannel().getName(), false);
        embed.addField("What Changed", event.getPropertyIdentifier(), false);
        embed.setColor(Color.ORANGE);
        logChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
