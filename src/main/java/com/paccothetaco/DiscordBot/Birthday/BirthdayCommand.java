package com.paccothetaco.DiscordBot.Birthday;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.DatabaseManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

public class BirthdayCommand {
    public static void handleSetBirthday(SlashCommandInteractionEvent event, DataManager dataManager) {
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();
        LocalDate birthday = LocalDate.parse(event.getOption("birthday").getAsString());

        if (!dataManager.isBirthdayActive(guildId)) {
            event.reply("Birthday reminders are not activated on the server").setEphemeral(true).queue();
            return;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "INSERT INTO birthdays (user_id, server_id, birthday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE birthday = VALUES(birthday)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userId);
                stmt.setString(2, guildId);
                stmt.setDate(3, Date.valueOf(birthday));
                stmt.executeUpdate();
                event.reply("Your birthday has been set to " + birthday).setEphemeral(true).queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("An error occurred while setting your birthday. Please try again later.").setEphemeral(true).queue();
        }
    }

    public static void handleDeleteBirthday(SlashCommandInteractionEvent event, DataManager dataManager) {
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        if (!dataManager.isBirthdayActive(guildId)) {
            event.reply("Birthday reminders are not activated on the server").setEphemeral(true).queue();
            return;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "DELETE FROM birthdays WHERE user_id = ? AND server_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userId);
                stmt.setString(2, guildId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    event.reply("Your birthday has been removed.").setEphemeral(true).queue();
                } else {
                    event.reply("No birthday found to delete.").setEphemeral(true).queue();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("An error occurred while deleting your birthday. Please try again later.").setEphemeral(true).queue();
        }
    }
}
