package com.paccothetaco.DiscordBot.Utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.paccothetaco.DiscordBot.DataManager;

public class LogUtil {
    public static TextChannel getLogChannel(Guild guild, DataManager dataManager) {
        String logChannelId = dataManager.getMessageLogChannel(guild.getId());
        return logChannelId != null ? guild.getTextChannelById(logChannelId) : null;
    }
}
