package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

public class NameChangeListener extends ListenerAdapter {
    private final DataManager dataManager;

    public NameChangeListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.isMember(event.getUser()) && dataManager.isChangeNameLogActive(guild.getId())) {
                TextChannel logChannel = LogUtil.getLogChannel(guild, dataManager);
                if (logChannel != null) {
                    logChannel.sendMessage(event.getUser().getAsTag() + " has changed their name from " +
                            event.getOldName() + " to " + event.getNewName() + ".").queue();
                }
            }
        });
    }
}
