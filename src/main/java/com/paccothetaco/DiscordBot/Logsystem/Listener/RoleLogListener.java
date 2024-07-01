package com.paccothetaco.DiscordBot.Logsystem.Listener;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.audit.ActionType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class RoleLogListener extends ListenerAdapter {
    private final DataManager dataManager;
    private static final Color EMBED_COLOR = Color.GREEN;

    public RoleLogListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRole();

        guild.retrieveAuditLogs().type(ActionType.ROLE_CREATE).limit(1).queue(entries -> {
            User creator = entries.isEmpty() ? null : entries.get(0).getUser();
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Role created")
                    .setColor(EMBED_COLOR)
                    .addField("Rolename", role.getAsMention(), false)
                    .addField("Created by", creator != null ? creator.getAsMention() : "Unknown", false)
                    .addField("Permissions", role.getPermissions().toString(), false);
            logMessage(guild, embed.build());
        });
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRole();
        String roleName = role.getName();

        guild.retrieveAuditLogs().type(ActionType.ROLE_DELETE).limit(1).queue(entries -> {
            User deleter = entries.isEmpty() ? null : entries.get(0).getUser();
            List<Member> membersWithRole = guild.getMembersWithRoles(role);
            StringBuilder membersString = new StringBuilder();
            if (membersWithRole.isEmpty()) {
                membersString.append("none");
            } else {
                for (Member member : membersWithRole) {
                    membersString.append(member.getAsMention()).append("\n");
                }
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Role deleted")
                    .setColor(EMBED_COLOR)
                    .addField("Rolename", roleName, false)
                    .addField("Deleted by", deleter != null ? deleter.getAsMention() : "Unknown", false)
                    .addField("Members with this Role", membersString.toString(), false);
            logMessage(guild, embed.build());
        });
    }

    @Override
    public void onRoleUpdateName(@NotNull RoleUpdateNameEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRole();
        String oldName = event.getOldName();
        String newName = event.getNewName();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Role changed")
                .setColor(EMBED_COLOR)
                .addField("Role", role.getAsMention(), false)
                .addField("Change Type", "Name", false)
                .addField("Old Name", oldName, false)
                .addField("New Name", newName, false);
        logMessage(guild, embed.build());
    }

    @Override
    public void onRoleUpdateColor(@NotNull RoleUpdateColorEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRole();
        Color oldColor = event.getOldColor();
        Color newColor = event.getNewColor();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Role changed")
                .setColor(EMBED_COLOR)
                .addField("Role", role.getAsMention(), false)
                .addField("Change Type", "Color", false)
                .addField("Old Color", oldColor == null ? "None" : "#" + Integer.toHexString(oldColor.getRGB()).substring(2).toUpperCase(), false)
                .addField("New Color", newColor == null ? "None" : "#" + Integer.toHexString(newColor.getRGB()).substring(2).toUpperCase(), false);
        logMessage(guild, embed.build());
    }

    @Override
    public void onRoleUpdatePermissions(@NotNull RoleUpdatePermissionsEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRole();
        List<String> addedPermissions = event.getNewPermissions().stream()
                .filter(perm -> !event.getOldPermissions().contains(perm))
                .map(Enum::name)
                .collect(Collectors.toList());
        List<String> removedPermissions = event.getOldPermissions().stream()
                .filter(perm -> !event.getNewPermissions().contains(perm))
                .map(Enum::name)
                .collect(Collectors.toList());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Role changed")
                .setColor(EMBED_COLOR)
                .addField("Role", role.getAsMention(), false)
                .addField("Change Type", "Permissions", false)
                .addField("Added Permissions", String.join(", ", addedPermissions), false)
                .addField("Removed Permissions", String.join(", ", removedPermissions), false);
        logMessage(guild, embed.build());
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRoles().get(0);  // Assuming only one role is added at a time
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Role added")
                .setColor(EMBED_COLOR)
                .addField("Member", member.getAsMention(), false)
                .addField("Added Role", role.getAsMention(), false);
        logMessage(guild, embed.build());
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();
        if (!dataManager.isRoleLogActive(guild.getId())) {
            return;
        }
        Role role = event.getRoles().get(0);  // Assuming only one role is removed at a time
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Role removed")
                .setColor(EMBED_COLOR)
                .addField("Member", member.getAsMention(), false)
                .addField("Removed Role", role.getAsMention(), false);
        logMessage(guild, embed.build());
    }

    private void logMessage(Guild guild, net.dv8tion.jda.api.entities.MessageEmbed embed) {
        String logChannelId = dataManager.getMessageLogChannel(guild.getId());
        if (logChannelId != null) {
            TextChannel logChannel = guild.getTextChannelById(logChannelId);
            if (logChannel != null) {
                logChannel.sendMessageEmbeds(embed).queue();
            }
        }
    }
}
