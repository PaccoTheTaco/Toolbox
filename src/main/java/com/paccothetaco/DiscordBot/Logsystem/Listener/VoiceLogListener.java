package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;

public class VoiceLogListener extends ListenerAdapter {
    private final long logChannelId = 1254398510439075910L;
    private DataManager dataManager;

    public VoiceLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (!dataManager.isVoiceChannelLogActive(event.getGuild().getId())) {
            return;
        }

        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);
        embedBuilder.setFooter(event.getMember().getEffectiveName());

        if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
            embedBuilder.setTitle("Member left Voice Channel");
            embedBuilder.addField("User", event.getMember().getAsMention(), false);
            embedBuilder.addField("Channel", event.getChannelLeft().getName(), false);
        } else if (event.getChannelLeft() == null && event.getChannelJoined() != null) {
            embedBuilder.setTitle("Member joined Voice Channel");
            embedBuilder.addField("User", event.getMember().getAsMention(), false);
            embedBuilder.addField("Channel", event.getChannelJoined().getName(), false);
        } else if (event.getChannelLeft() != null && event.getChannelJoined() != null) {
            embedBuilder.setTitle("Member switched Voice Channel");
            embedBuilder.addField("User", event.getMember().getAsMention(), false);
            embedBuilder.addField("Before", event.getChannelLeft().getName(), false);
            embedBuilder.addField("After", event.getChannelJoined().getName(), false);
        }
        embedBuilder.setFooter(event.getMember().getEffectiveName());
        embedBuilder.setThumbnail(event.getMember().getUser().getAvatarUrl());

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
