package com.paccothetaco.DiscordBot.Ticketsystem.command;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Utils.SelectMenuUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketEmbedCommand {
    private final DataManager dataManager;

    public TicketEmbedCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void execute(SlashCommandInteractionEvent event) {
        sendNewTicketEmbed(event, false);
    }

    public void sendNewTicketEmbed(SlashCommandInteractionEvent event, boolean isUpdate) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
            String guildId = event.getGuild().getId();
            TextChannel channel = (TextChannel) event.getChannel();
            sendNewTicketEmbed(channel, guildId, isUpdate);
            if (!isUpdate) event.reply("Ticket embed sent.").setEphemeral(true).queue();
        } else {
            event.reply("You don't have permission to perform this.").setEphemeral(true).queue();
        }
    }

    // Neue Methode mit 3 Argumenten
    public void sendNewTicketEmbed(TextChannel channel, String guildId, boolean isUpdate) {
        Map<String, Boolean> ticketOptions = dataManager.getTicketOptions(guildId);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Tickets");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setFooter("by paccothetaco.com");
        embedBuilder.setDescription("Do you have a question, want to report a user or apply? \nThen choose one of the following options:");

        List<String> options = new ArrayList<>();
        if (ticketOptions.getOrDefault("support", true)) {
            options.add("support");
        }
        if (ticketOptions.getOrDefault("report", true)) {
            options.add("report");
        }
        if (ticketOptions.getOrDefault("application", true)) {
            options.add("application");
        }

        MessageEmbed embed = embedBuilder.build();

        // Löschen des alten Embeds, wenn ein Update stattfindet
        if (isUpdate) {
            dataManager.deleteOldTicketEmbed(guildId, channel);
        }

        // Neues Embed senden und die Nachricht ID speichern
        channel.sendMessageEmbeds(embed).setActionRow(SelectMenuUtil.ticketSelect(options)).queue(message -> {
            dataManager.setTicketEmbedMessageId(guildId, message.getId());
        });
    }
}
