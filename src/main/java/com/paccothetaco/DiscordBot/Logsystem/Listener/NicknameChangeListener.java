package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.Utils.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.Color;

public class NicknameChangeListener extends ListenerAdapter {
    private final DataManager dataManager;

    public NicknameChangeListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        // Debugging: Ausgabe zur Überprüfung, ob das Event abgefangen wird
        System.out.println("Nickname change event detected for user: " + event.getUser().getAsTag());

        // Überprüfe, ob der Bot Berechtigungen hat, um die Änderung zu protokollieren
        if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_SEND)) {
            if (dataManager.isChangeNicknameLogActive(event.getGuild().getId())) {
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

                    logChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        } else {
            System.err.println("Bot lacks permission to write messages in the specified channel.");
        }
    }
}
