package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.CommandUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Random;

public class WelcomeAndLeave extends ListenerAdapter {
    private CommandUtil commandUtil;
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

    public WelcomeAndLeave(CommandUtil commandUtil) {
        this.commandUtil = commandUtil;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        String welcomeChannelId = commandUtil.getWelcomeChannelId(event.getGuild().getId());
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

                welcomeChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        String leaveChannelId = commandUtil.getLeaveChannelId(event.getGuild().getId());
        if (leaveChannelId != null) {
            TextChannel leaveChannel = event.getGuild().getTextChannelById(leaveChannelId);
            if (leaveChannel != null) {
                int memberCount = event.getGuild().getMemberCount();
                String leaveMessage = String.format("Goodbye, %s. We hope to see you again!\nNow we are down to %d members.", event.getUser().getAsTag(), memberCount);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);
                embed.setDescription(leaveMessage);
                embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                embed.setFooter("We hope to see you back!", event.getGuild().getIconUrl());

                leaveChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
