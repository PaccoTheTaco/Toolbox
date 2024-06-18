package com.paccothetaco.DiscordBot.Ticketsystem.command;

import com.paccothetaco.DiscordBot.Utils.SelectMenuUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

public class TicketEmbedCommand {

    public static void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Tickets");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setFooter("by paccothetaco.com");
            embedBuilder.setDescription("Du hast eine Frage, möchtest einen Benutzer melden oder dich bewerben? Dann wähle eine der folgenden Optionen:");
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(SelectMenuUtil.ticketSelect()).queue();
            event.reply("Embed was sent").setEphemeral(true).queue();
        } else {
            event.reply("You don't have permission to perform this.").setEphemeral(true).queue();
        }
    }
}
