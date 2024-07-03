package com.paccothetaco.DiscordBot.Giveaway;

import com.paccothetaco.DiscordBot.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Giveaways extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("startgiveaway")) {
            String title = event.getOption("title").getAsString();
            String price = event.getOption("price").getAsString();
            String howLong = event.getOption("howlong").getAsString();
            String howToReact = event.getOption("howtoreact").getAsString();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(title);
            embed.setDescription("Price: " + price + "\nReact with " + howToReact + " to enter the giveaway.\nThe giveaway runs until " + Instant.now().plusSeconds(Long.parseLong(howLong)));
            embed.setColor(Color.ORANGE);

            event.replyEmbeds(embed.build()).queue(response -> {
                response.retrieveOriginal().queue(message -> {
                    long serverId = event.getGuild().getIdLong();
                    long messageId = message.getIdLong();

                    try (Connection connection = DatabaseManager.getConnection()) {
                        PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO giveaways (server_id, giveaway_active, price, giveaway_message_id) VALUES (?, ?, ?, ?)"
                        );
                        statement.setLong(1, serverId);
                        statement.setBoolean(2, true);
                        statement.setString(3, price);
                        statement.setLong(4, messageId);
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    message.addReaction(Emoji.fromUnicode(howToReact)).queue();

                    // Schedule end of giveaway
                    scheduler.schedule(() -> endGiveaway(serverId), Long.parseLong(howLong), TimeUnit.SECONDS);
                });
            });
        }
    }

    private void endGiveaway(long serverId) {
        // Implementation to end the giveaway, select winner, etc.
    }
}
