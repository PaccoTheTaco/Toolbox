package com.paccothetaco.DiscordBot.LinkTwitch;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class Twitchlinking {
    private static JDA jda;

    public static void initialize(JDA jdaInstance) {
        jda = jdaInstance;
    }

    public static void onTwitchLive(String twitchUsername, String streamLink) {
        DataManager dataManager = new DataManager();
        List<DataManager.TwitchLink> links = dataManager.getTwitchLinks();

        for (DataManager.TwitchLink link : links) {
            if (link.getTwitchUsername().equalsIgnoreCase(twitchUsername)) {
                String guildId = link.getGuildId();
                String channelId = link.getDiscordChannelId();

                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel != null) {
                    channel.sendMessage(twitchUsername + " is now live. Watch here: " + streamLink).queue();
                }
            }
        }
    }
}
