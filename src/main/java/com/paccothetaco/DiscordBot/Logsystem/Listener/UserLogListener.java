package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.Color;

public class UserLogListener extends ListenerAdapter {
    private final DataManager dataManager;

    public UserLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.isMember(event.getUser()) && dataManager.isUserLogActive(guild.getId())) {
                TextChannel logChannel = LogUtil.getLogChannel(guild, dataManager);
                if (logChannel != null) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN)
                            .setTitle("Name Change")
                            .addField("User", event.getUser().getAsMention(), false)
                            .addField("From", event.getOldName(), false)
                            .addField("To", event.getNewName(), false);
                    embed.setThumbnail(event.getUser().getAvatarUrl());
                    embed.setFooter(event.getUser().getName());
                    logChannel.sendMessageEmbeds(embed.build()).queue();
                } else {
                    System.err.println("Log channel is null for guild: " + guild.getId());
                }
            } else {
                System.err.println("User is not a member or user log is not active for guild: " + guild.getId());
            }
        });
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        System.out.println("Nickname change event detected for user: " + event.getUser().getAsTag());

        if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_SEND)) {
            if (dataManager.isUserLogActive(event.getGuild().getId())) {
                TextChannel logChannel = LogUtil.getLogChannel(event.getGuild(), dataManager);
                if (logChannel != null) {
                    String oldNickname = event.getOldNickname() != null ? event.getOldNickname() : event.getMember().getEffectiveName();
                    String newNickname = event.getNewNickname() != null ? event.getNewNickname() : event.getMember().getEffectiveName();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN)
                            .setTitle("Nickname Change")
                            .addField("User", event.getMember().getUser().getAsMention(), false)
                            .addField("From", oldNickname, false)
                            .addField("To", newNickname, false);
                    embed.setThumbnail(event.getUser().getAvatarUrl());
                    embed.setFooter(event.getUser().getName());
                    logChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        } else {
            System.err.println("Bot lacks permission to write messages in the specified channel.");
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.isMember(event.getUser()) && dataManager.isUserLogActive(guild.getId())) {
                TextChannel logChannel = LogUtil.getLogChannel(guild, dataManager);
                if (logChannel != null) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN)
                            .setTitle("Avatar Change")
                            .addField("User", event.getUser().getAsMention(), false)
                            .setThumbnail(event.getUser().getAvatarUrl());
                    embed.setThumbnail(event.getUser().getAvatarUrl());
                    embed.setFooter(event.getUser().getName());
                    logChannel.sendMessageEmbeds(embed.build()).queue();
                } else {
                    System.err.println("Log channel is null for guild: " + guild.getId());
                }
            } else {
                System.err.println("User is not a member or user log is not active for guild: " + guild.getId());
            }
        });
    }
}
