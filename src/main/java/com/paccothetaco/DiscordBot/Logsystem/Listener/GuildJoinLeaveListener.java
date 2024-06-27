package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

public class GuildJoinLeaveListener extends ListenerAdapter {
    private final DataManager dataManager;

    public GuildJoinLeaveListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (dataManager.isJoinLogActive(event.getGuild().getId())) {
            TextChannel logChannel = LogUtil.getLogChannel(event.getGuild(), dataManager);
            if (logChannel != null) {
                logChannel.sendMessage(event.getMember().getUser().getAsTag() + " has joined the server.").queue();
            }
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (dataManager.isLeaveLogActive(event.getGuild().getId())) {
            TextChannel logChannel = LogUtil.getLogChannel(event.getGuild(), dataManager);
            if (logChannel != null) {
                logChannel.sendMessage(event.getUser().getAsTag() + " has left the server.").queue();
            }
        }
    }
}
