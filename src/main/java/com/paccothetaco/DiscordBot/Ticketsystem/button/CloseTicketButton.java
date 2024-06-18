package com.paccothetaco.DiscordBot.Ticketsystem.button;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.util.List;

public class CloseTicketButton {
    public static void execute(ButtonInteractionEvent event, DataManager dataManager) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || member == null) return;

        String modRoleId = dataManager.getModRole(guild.getId());
        Role modRole = modRoleId != null ? guild.getRoleById(modRoleId) : null;
        List<Role> roles = member.getRoles();
        TextChannel textChannel = event.getChannel().asTextChannel();

        boolean hasPermission = false;
        if (modRole != null && roles.contains(modRole)) {
            hasPermission = true;
        }
        if (member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            hasPermission = true;
        }
        if (textChannel.getName().contains(member.getId())) {
            hasPermission = true;
        }

        if (!hasPermission) {
            event.reply("You can't close the Ticket").setEphemeral(true).queue();
            return;
        }

        // Remove all components (buttons) from the original message
        event.getInteraction().getMessage().editMessageComponents().queue();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Ticket")
                .setColor(Color.RED)
                .setFooter("by paccothetaco.com")
                .setDescription("The Ticket is closed");
        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        String[] nameParts = textChannel.getName().split("-");
        String newName = "closed-" + String.join("-", nameParts);

        textChannel.getManager().setName(newName).queue(success -> {
            // Check if the closed ticket category exists, if not, create it
            String closedTicketCategoryId = dataManager.getClosedTicketCategory(guild.getId());
            Category closedTicketsCategory = null;
            if (closedTicketCategoryId == null) {
                closedTicketsCategory = guild.createCategory("Closed-Tickets").complete();
                dataManager.setClosedTicketCategory(guild.getId(), closedTicketsCategory.getId());
            } else {
                closedTicketsCategory = guild.getCategoryById(closedTicketCategoryId);
                if (closedTicketsCategory == null) {
                    closedTicketsCategory = guild.createCategory("Closed-Tickets").complete();
                    dataManager.setClosedTicketCategory(guild.getId(), closedTicketsCategory.getId());
                }
            }

            // Move the text channel to the closed tickets category
            if (closedTicketsCategory != null) {
                textChannel.getManager().setParent(closedTicketsCategory).queue(success2 -> {
                    // Remove all permission overrides for the channel
                    textChannel.getMemberPermissionOverrides().forEach(permissionOverride -> permissionOverride.delete().queue());

                    // If modRole is null, ping @everyone with the message
                    if (modRole == null) {
                        textChannel.sendMessage("@everyone The moderator role is not set!").queue();
                    }
                }, error -> {
                    // Handle errors when setting the parent category
                    event.reply("Failed to move the ticket to the closed category.").setEphemeral(true).queue();
                });
            }
        }, error -> {
            // Handle errors when renaming the channel
            event.reply("Failed to rename the ticket channel.").setEphemeral(true).queue();
        });
    }
}
