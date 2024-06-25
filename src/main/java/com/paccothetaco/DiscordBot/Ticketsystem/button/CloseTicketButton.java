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

        String[] nameParts = textChannel.getName().split("-");
        String userId = nameParts.length > 1 ? nameParts[1] : null;
        String userMention = userId != null ? "<@" + userId + ">" : "unknown user";

        event.deferEdit().queue();
        event.getMessage().delete().queue();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Closed " + textChannel.getName().split("-")[0] + " Ticket")
                .setColor(Color.RED)
                .setDescription("Ticket created by: " + userMention + "\n" +
                        "Ticket closed by: " + member.getAsMention())
                .setFooter("by paccothetaco.com");

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

        String newName = "closed-" + String.join("-", nameParts);

        textChannel.getManager().setName(newName).queue(success -> {
            String closedTicketCategoryId = dataManager.getClosedTicketCategory(guild.getId());
            if (closedTicketCategoryId == null) {
                guild.createCategory("Closed-Tickets").queue(closedTicketsCategory -> {
                    dataManager.setClosedTicketCategory(guild.getId(), closedTicketsCategory.getId());
                    moveToClosedCategory(textChannel, closedTicketsCategory, event, modRole);
                });
            } else {
                Category closedTicketsCategory = guild.getCategoryById(closedTicketCategoryId);
                if (closedTicketsCategory == null) {
                    guild.createCategory("Closed-Tickets").queue(newClosedTicketsCategory -> {
                        dataManager.setClosedTicketCategory(guild.getId(), newClosedTicketsCategory.getId());
                        moveToClosedCategory(textChannel, newClosedTicketsCategory, event, modRole);
                    });
                } else {
                    moveToClosedCategory(textChannel, closedTicketsCategory, event, modRole);
                }
            }
        }, error -> {
            event.getHook().sendMessage("Failed to rename the ticket channel.").setEphemeral(true).queue();
        });
    }

    private static void moveToClosedCategory(TextChannel textChannel, Category closedTicketsCategory, ButtonInteractionEvent event, Role modRole) {
        textChannel.getManager().setParent(closedTicketsCategory).queue(success2 -> {
            textChannel.getMemberPermissionOverrides().forEach(permissionOverride -> permissionOverride.delete().queue());

            if (modRole == null) {
                textChannel.sendMessage("@everyone The moderator role is not set!").queue();
            }

            event.getHook().sendMessage("Ticket successfully closed.").setEphemeral(true).queue();
        }, error -> {
            event.getHook().sendMessage("Failed to move the ticket to the closed category.").setEphemeral(true).queue();
        });
    }
}
