package com.paccothetaco.DiscordBot;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GiveawayReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        long messageId = event.getMessageIdLong();
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT server_id FROM giveaways WHERE giveaway_message_id = ? AND giveaway_active = TRUE"
            );
            statement.setLong(1, messageId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                long serverId = rs.getLong("server_id");
                addGiveawayParticipant(serverId, event.getUserIdLong());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addGiveawayParticipant(long serverId, long userId) {
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO giveaway_participants (server_id, user_id) VALUES (?, ?)"
            );
            statement.setLong(1, serverId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
