package com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Random;

public class WelcomeAndLeave extends ListenerAdapter implements DataManager.DataChangeListener {
    private DataManager channelDataManager;
    private String[] welcomeMessages = {
            "Hey %s, great to have you here!",
            "Hello %s, we're glad you found your way here.",
            "Heyho %s, we're happy to have you with us.",
            "%s, welcome to %s!",
            "Welcome %s! Enjoy your stay at %s.",
            "Hi %s, we're excited to see you here!",
            "Greetings %s, welcome aboard!",
            "Hey %s, we've been waiting for you!"
    };

    public WelcomeAndLeave(DataManager channelDataManager) {
        this.channelDataManager = channelDataManager;
        this.channelDataManager.addListener(this);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!channelDataManager.isWelcomeActive(event.getGuild().getId())) {
            return;
        }

        String welcomeChannelId = channelDataManager.getWelcomeChannelId(event.getGuild().getId());
        if (welcomeChannelId != null) {
            TextChannel welcomeChannel = event.getGuild().getTextChannelById(welcomeChannelId);
            if (welcomeChannel != null) {
                Random rand = new Random();
                String welcomeMessage = String.format(welcomeMessages[rand.nextInt(welcomeMessages.length)], event.getMember().getAsMention(), event.getGuild().getName());
                int memberCount = event.getGuild().getMemberCount();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.CYAN);
                embed.setDescription(welcomeMessage + "\n\nYou are the " + memberCount + "th member on this server.");
                embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                embed.setFooter("Welcome to the community!", event.getGuild().getIconUrl());

                welcomeChannel.sendMessageEmbeds(embed.build()).queue(
                        success -> System.out.println("Welcome message sent successfully."),
                        error -> System.err.println("Failed to send welcome message: " + error)
                );
            } else {
                System.out.println("Welcome channel not found.");
            }
        } else {
            System.out.println("No welcome channel ID set.");
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!channelDataManager.isLeaveActive(event.getGuild().getId())) {
            return;
        }

        String leaveChannelId = channelDataManager.getLeaveChannelId(event.getGuild().getId());
        System.out.println("Leave channel ID: " + leaveChannelId);
        if (leaveChannelId != null) {
            TextChannel leaveChannel = event.getGuild().getTextChannelById(leaveChannelId);
            if (leaveChannel != null) {
                System.out.println("Leave channel found: " + leaveChannel.getName());
                int memberCount = event.getGuild().getMemberCount();
                String leaveMessage = String.format("Goodbye, %s. We hope to see you again!\nNow we are down to %d members.", event.getUser().getAsTag(), memberCount);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);
                embed.setDescription(leaveMessage);
                embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                embed.setFooter("We hope to see you back!", event.getGuild().getIconUrl());

                leaveChannel.sendMessageEmbeds(embed.build()).queue(
                        success -> System.out.println("Leave message sent successfully."),
                        error -> System.err.println("Failed to send leave message: " + error)
                );
            } else {
                System.out.println("Leave channel not found.");
            }
        } else {
            System.out.println("No leave channel ID set.");
        }
    }

    @Override
    public void onDataChanged(String guildId) {
        System.out.println("Data changed for guild: " + guildId);
    }
}
