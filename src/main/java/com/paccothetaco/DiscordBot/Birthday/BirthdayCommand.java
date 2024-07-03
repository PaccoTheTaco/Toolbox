package com.paccothetaco.DiscordBot.Birthday;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class BirthdayCommand {

    private static final ZoneId MEZ = ZoneId.of("Europe/Berlin");
    private static LocalDate lastCheckedDate = LocalDate.now(MEZ);

    public static void handleSetBirthday(SlashCommandInteractionEvent event, DataManager dataManager) {
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();
        int day = event.getOption("day").getAsInt();
        int month = event.getOption("month").getAsInt();
        int currentYear = LocalDate.now(MEZ).getYear();
        LocalDate birthday = LocalDate.of(currentYear, month, day); // Verwenden Sie das aktuelle Jahr

        if (!dataManager.isBirthdayActive(guildId)) {
            event.reply("Birthday reminders are not activated on the server").setEphemeral(true).queue();
            return;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "INSERT INTO birthdays (user_id, server_id, birthday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE birthday = VALUES(birthday)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userId);
                stmt.setString(2, guildId);
                stmt.setDate(3, java.sql.Date.valueOf(birthday));
                stmt.executeUpdate();
                event.reply("Your birthday has been set to " + day + "/" + month).setEphemeral(true).queue();
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

    public static void scheduleBirthdayCheck(JDA jda, DataManager dataManager) {
        Timer timer = new Timer();
        ZonedDateTime now = ZonedDateTime.now(MEZ);
        ZonedDateTime nextRun = now.withHour(1).withMinute(30).withSecond(0);

        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkBirthdays(jda, dataManager);
            }
        }, java.util.Date.from(nextRun.toInstant()), 24 * 60 * 60 * 1000);

        if (lastCheckedDate.isBefore(LocalDate.now(MEZ))) {
            checkBirthdays(jda, dataManager);
        }
    }

    public static synchronized void checkBirthdays(JDA jda, DataManager dataManager) {
        LocalDate today = LocalDate.now(MEZ);
        System.out.println("Checking birthdays for " + today);

        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "SELECT user_id, server_id FROM birthdays WHERE MONTH(birthday) = ? AND DAY(birthday) = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, today.getMonthValue());
                stmt.setInt(2, today.getDayOfMonth());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String userId = rs.getString("user_id");
                        String serverId = rs.getString("server_id");
                        System.out.println("Found birthday for userId: " + userId + ", serverId: " + serverId);
                        processBirthday(jda, dataManager, userId, serverId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void processBirthday(JDA jda, DataManager dataManager, String userId, String serverId) {
        if (dataManager.isBirthdayActive(serverId)) {
            String channelId = dataManager.getBirthdayChannelId(serverId);
            TextChannel channel = jda.getTextChannelById(channelId);
            User user = jda.retrieveUserById(userId).complete();

            if (channel != null && user != null) {
                sendBirthdayMessage(channel, user);
            } else {
                System.out.println("Channel or user not found. Channel: " + (channel != null) + ", User: " + (user != null));
            }
        } else {
            System.out.println("Birthday reminders are not active for serverId: " + serverId);
        }
    }

    private static void sendBirthdayMessage(TextChannel channel, User user) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("HAPPY BIRTHDAY :confetti_ball: :tada:");
        embed.setDescription("Happy birthday " + user.getAsMention() + ", good luck in your new phase of life!");
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.setFooter(user.getName(), user.getEffectiveAvatarUrl());
        embed.setColor(Color.ORANGE);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public static void handleTestBirthday(SlashCommandInteractionEvent event, DataManager dataManager) {
        event.deferReply().setEphemeral(true).queue();
        checkBirthdays(event.getJDA(), dataManager);
        event.getHook().sendMessage("Birthday check executed.").setEphemeral(true).queue();
    }
}
