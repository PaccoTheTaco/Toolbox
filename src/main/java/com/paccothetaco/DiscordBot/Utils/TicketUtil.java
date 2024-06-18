package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Ticketsystem.TicketType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketUtil {
    public static void open(TicketType ticketType, StringSelectInteractionEvent event, DataManager dataManager) {
        String guildId = event.getGuild().getId();
        String ticketCategoryId = dataManager.getTicketCategory(guildId);

        // Check if the ticket category exists, if not, create it
        Category ticketCategory = null;
        if (ticketCategoryId == null) {
            ticketCategory = event.getGuild().createCategory("Tickets").complete();
            dataManager.setTicketCategory(guildId, ticketCategory.getId());
        } else {
            ticketCategory = event.getGuild().getCategoryById(ticketCategoryId);
            if (ticketCategory == null) {
                ticketCategory = event.getGuild().createCategory("Tickets").complete();
                dataManager.setTicketCategory(guildId, ticketCategory.getId());
            }
        }

        // Create the ticket channel
        String channelName = ticketType.name().toLowerCase() + "-" + event.getUser().getId();
        TextChannel textChannel = event.getGuild().createTextChannel(channelName)
                .setParent(ticketCategory)
                .addPermissionOverride(event.getGuild().getPublicRole(), 0, 1024) // Set appropriate permissions
                .complete();

        // Create the embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title;
        String description = "";
        String mention = event.getMember().getAsMention();
        Role modRole = null;
        String modMention = "Moderator role not set";

        String modRoleId = dataManager.getModRole(guildId);
        if (modRoleId != null) {
            modRole = event.getGuild().getRoleById(modRoleId);
            if (modRole != null) {
                modMention = modRole.getAsMention();
            }
        }

        switch (ticketType) {
            case SUPPORT -> {
                title = "Support Ticket";
                description = "Please write your issue in this channel.";
            }
            case REPORT -> {
                title = "Report Ticket";
                description = "Please write your report in this channel.";
            }
            case APPLICATION -> {
                title = "Application Ticket";
                description = "Please write your application in this channel.";
            }
            default -> {
                event.reply("This option can't open a ticket. Contact the support.").setEphemeral(true).queue();
                return;
            }
        }

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setFooter("by paccothetaco.com");
        embedBuilder.setDescription(description + "\nA " + modMention + " will assist you shortly.\nTicket created by: " + mention);

        // Send the embed in the channel with the close button
        textChannel.sendMessageEmbeds(embedBuilder.build())
                .setActionRow(Button.danger("close-ticket", "Close Ticket"))
                .queue();

        // Ping the mod role if it exists
        if (modRole != null) {
            textChannel.sendMessage(modRole.getAsMention()).queue(message -> {
                // Schedule the deletion of the message after 1 second
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> message.delete().queue(), 1, TimeUnit.SECONDS);
            });
        }

        event.reply("Ticket created successfully.").setEphemeral(true).queue();
    }
}
