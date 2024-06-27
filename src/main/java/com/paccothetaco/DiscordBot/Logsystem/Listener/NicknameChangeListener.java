package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

public class NicknameChangeListener extends ListenerAdapter {
    private final DataManager dataManager;

    public NicknameChangeListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (dataManager.isChangeNicknameLogActive(event.getGuild().getId())) {
            TextChannel logChannel = LogUtil.getLogChannel(event.getGuild(), dataManager);
            if (logChannel != null) {
                logChannel.sendMessage(event.getMember().getUser().getAsTag() + " has changed their nickname from " +
                        event.getOldNickname() + " to " + event.getNewNickname() + ".").queue();
            }
        }
    }
}
