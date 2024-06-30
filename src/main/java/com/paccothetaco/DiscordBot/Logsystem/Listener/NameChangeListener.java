package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.Color;

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
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN)
                            .setTitle("Name Change")
                            .addField("User", event.getUser().getAsMention(), false)
                            .addField("From", event.getOldName(), false)
                            .addField("To", event.getNewName(), false);

                    logChannel.sendMessageEmbeds(embed.build()).queue();
                } else {
                    System.err.println("Log channel is null for guild: " + guild.getId());
                }
            } else {
                System.err.println("User is not a member or name change log is not active for guild: " + guild.getId());
            }
        });
    }
}
