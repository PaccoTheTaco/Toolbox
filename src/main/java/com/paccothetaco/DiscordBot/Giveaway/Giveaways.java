package com.paccothetaco.DiscordBot.Giveaway;

import com.paccothetaco.DiscordBot.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Giveaways extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "startgiveaway":
                handleStartGiveaway(event);
                break;
            case "endgiveaway":
                handleEndGiveaway(event);
                break;
        }
    }

    private void handleStartGiveaway(SlashCommandInteractionEvent event) {
        String title = event.getOption("title").getAsString();
        String price = event.getOption("price").getAsString();
        String howLong = event.getOption("howlong").getAsString();
        String howToReact = event.getOption("howtoreact").getAsString();
        long duration = Long.parseLong(howLong);

        // Use the system default timezone
        ZoneId zoneId = ZoneId.systemDefault();
        Instant endTime = Instant.now().plus(duration, ChronoUnit.SECONDS);
        String formattedEndTime = LocalDateTime.ofInstant(endTime, zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timeZone = TimeZone.getTimeZone(zoneId).getDisplayName();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription("Price: " + price + "\nReact with " + howToReact + " to enter the giveaway.\nThe giveaway ends at: " + formattedEndTime + " (" + timeZone + ")");
        embed.setColor(Color.ORANGE);

        long serverId = event.getGuild().getIdLong();
        boolean giveawayActive = false;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT giveaway_active FROM giveaways WHERE server_id = ?"
            );
            checkStatement.setLong(1, serverId);
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next()) {
                giveawayActive = rs.getBoolean("giveaway_active");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("An error occurred while checking for an active giveaway.").queue();
            return;
        }

        if (giveawayActive) {
            event.reply("A giveaway is already active on this server.").queue();
            return;
        }

        event.deferReply().queue(hook -> {
            event.getChannel().sendMessageEmbeds(embed.build()).queue(message -> {
                long messageId = message.getIdLong();

                try (Connection connection = DatabaseManager.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO giveaways (server_id, giveaway_active, price, giveaway_message_id, reaction_emoji) VALUES (?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE giveaway_active = VALUES(giveaway_active), price = VALUES(price), giveaway_message_id = VALUES(giveaway_message_id), reaction_emoji = VALUES(reaction_emoji)"
                    );
                    statement.setLong(1, serverId);
                    statement.setBoolean(2, true);
                    statement.setString(3, price);
                    statement.setLong(4, messageId);
                    statement.setString(5, howToReact);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    message.delete().queue();
                    hook.editOriginal("An error occurred while starting the giveaway.").queue();
                    return;
                }

                message.addReaction(Emoji.fromUnicode(howToReact)).queue();
                scheduler.schedule(() -> endGiveaway(event.getChannel(), serverId, messageId, price, howToReact), duration, TimeUnit.SECONDS);
                hook.editOriginal("The giveaway has been started!").queue();
            });
        });
    }

    private void handleEndGiveaway(SlashCommandInteractionEvent event) {
        long serverId = event.getGuild().getIdLong();

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT giveaway_message_id, price, reaction_emoji FROM giveaways WHERE server_id = ? AND giveaway_active = ?"
            );
            statement.setLong(1, serverId);
            statement.setBoolean(2, true);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                long messageId = rs.getLong("giveaway_message_id");
                String price = rs.getString("price");
                String reactionEmoji = rs.getString("reaction_emoji");
                endGiveaway(event.getChannel(), serverId, messageId, price, reactionEmoji);
                event.reply("The giveaway has been ended!").queue();
            } else {
                event.reply("No active giveaway found to end.").queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("An error occurred while ending the giveaway.").queue();
        }
    }

    private void endGiveaway(MessageChannel channel, long serverId, long messageId, String price, String howToReact) {
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE giveaways SET giveaway_active = ? WHERE server_id = ?"
            );
            statement.setBoolean(1, false);
            statement.setLong(2, serverId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Select a random winner
        Message message = channel.retrieveMessageById(messageId).complete();
        List<User> users = message.retrieveReactionUsers(Emoji.fromUnicode(howToReact)).complete();
        users.remove(channel.getJDA().getSelfUser()); // Remove the bot itself if it reacted

        if (users.isEmpty()) {
            message.getChannel().sendMessage("No participants for the giveaway.").queue();
            return;
        }

        Random random = new Random();
        User winner = users.get(random.nextInt(users.size()));

        // Update the message to show the winner
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Giveaway Ended!");
        embed.setDescription("Winner: " + winner.getAsMention() + "\nPrize: " + price);
        embed.setColor(Color.GREEN);

        message.editMessageEmbeds(embed.build()).queue();
        message.getChannel().sendMessage("Congratulations " + winner.getAsMention() + "! You have won the giveaway for: " + price).queue();
    }
}
