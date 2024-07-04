package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Reactionroles.ReactionRole;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private List<DataChangeListener> listeners = new ArrayList<>();

    public void checkAndAddServerIDs(JDA jda) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectQuery = "SELECT COUNT(*) FROM server_data WHERE Server_ID = ?";
            String insertQuery = "INSERT INTO server_data (Server_ID, welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, ticket_channel_ID, tickets_active, userlog_active, voice_channel_log_active, Channel_Log_active, ModLog_active, RoleLog_active, server_log_active, message_log_active) VALUES (?, NULL, NULL, false, false, NULL, NULL, NULL, NULL, true, true, true, NULL, NULL, false, false, false, false, false, false, false, false)";

            for (Guild guild : jda.getGuilds()) {
                String serverID = guild.getId();

                try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                    selectStmt.setString(1, serverID);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                                insertStmt.setString(1, serverID);
                                insertStmt.executeUpdate();
                                System.out.println("Added server ID: " + serverID);
                                notifyListeners(serverID);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addReactionRole(String guildId, String messageId, String roleId, String emoji) {
        String query = "INSERT INTO reaction_roles (guild_id, message_id, role_id, emoji) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, guildId);
            stmt.setString(2, messageId);
            stmt.setString(3, roleId);
            stmt.setString(4, emoji);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ReactionRole> getReactionRoles(String messageId) {
        String query = "SELECT guild_id, message_id, role_id, emoji FROM reaction_roles WHERE message_id = ?";
        List<ReactionRole> roles = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(new ReactionRole(
                            rs.getString("guild_id"),
                            rs.getString("message_id"),
                            rs.getString("role_id"),
                            rs.getString("emoji")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public void removeReactionRole(String messageId, String emoji) {
        String query = "DELETE FROM reaction_roles WHERE message_id = ? AND emoji = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, messageId);
            stmt.setString(2, emoji);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addServerOnJoin(Guild guild) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectQuery = "SELECT COUNT(*) FROM server_data WHERE Server_ID = ?";
            String insertQuery = "INSERT INTO server_data (Server_ID, welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, ticket_channel_ID, tickets_active, userlog_active, voice_channel_log_active, Channel_Log_active, ModLog_active, RoleLog_active, server_log_active, message_log_active) VALUES (?, NULL, NULL, false, false, NULL, NULL, NULL, NULL, true, true, true, NULL, NULL, false, false, false, false, false, false, false, false)";

            String serverID = guild.getId();

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setString(1, serverID);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                            insertStmt.setString(1, serverID);
                            insertStmt.executeUpdate();
                            System.out.println("Added server ID: " + serverID);
                            notifyListeners(serverID);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void setTicketEmbedMessageId(String guildId, String messageId) {
        updateServerData(guildId, "ticketembed_message_id", messageId);
    }

    public String getTicketEmbedMessageId(String guildId) {
        return getServerData(guildId).getTicketEmbedMessageId();
    }

    public void notifyListeners(String guildId) {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(guildId);
        }
    }

    public void setWelcomeChannel(String guildId, String channelId) {
        updateServerData(guildId, "welcome_channel_ID", channelId);
    }

    public void setLeaveChannel(String guildId, String channelId) {
        updateServerData(guildId, "leave_channel_ID", channelId);
    }

    public void setWelcomeActive(String guildId, boolean isActive) {
        updateServerData(guildId, "welcome_active", isActive);
    }

    public void setLeaveActive(String guildId, boolean isActive) {
        updateServerData(guildId, "leave_active", isActive);
    }

    public String getWelcomeChannelId(String guildId) {
        return getServerData(guildId).getWelcomeChannelId();
    }

    public String getLeaveChannelId(String guildId) {
        return getServerData(guildId).getLeaveChannelId();
    }

    public boolean isWelcomeActive(String guildId) {
        return getServerData(guildId).isWelcomeActive();
    }

    public boolean isLeaveActive(String guildId) {
        return getServerData(guildId).isLeaveActive();
    }

    public void setTicketCategory(String guildId, String categoryId) {
        updateServerData(guildId, "ticket_category_ID", categoryId);
    }

    public String getTicketCategory(String guildId) {
        return getServerData(guildId).getTicketCategoryId();
    }

    public void setClosedTicketCategory(String guildId, String categoryId) {
        updateServerData(guildId, "closed_ticket_category_ID", categoryId);
    }

    public String getClosedTicketCategory(String guildId) {
        return getServerData(guildId).getClosedTicketCategoryId();
    }

    public void setModRole(String guildId, String roleId) {
        updateServerData(guildId, "mod_role_ID", roleId);
    }

    public String getModRole(String guildId) {
        return getServerData(guildId).getModRoleId();
    }

    public void setMessageLogChannel(String guildId, String channelId) {
        updateServerData(guildId, "message_log_channel_ID", channelId);
    }

    public void deactivateMessageLog(String guildId) {
        updateServerData(guildId, "message_log_channel_ID", null);
    }

    public String getMessageLogChannel(String guildId) {
        return getServerData(guildId).getMessageLogChannelId();
    }

    public String getVoiceLogChannel(String guildId) {
        return getServerData(guildId).getVoiceLogChannelId();
    }

    public void setRoleLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "RoleLog_active", isActive);
    }

    public boolean isRoleLogActive(String guildId) {
        return getServerData(guildId).isRoleLogActive();
    }

    public void setServerLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "server_log_active", isActive);
    }

    public boolean isServerLogActive(String guildId) {
        return getServerData(guildId).isServerLogActive();
    }

    public void setTicketOption(String guildId, String option, boolean active) {
        updateServerData(guildId, option + "_ticket_active", active);
    }

    public void setModLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "ModLog_active", isActive);
    }

    public boolean isModLogActive(String guildId) {
        return getServerData(guildId).isModLogActive();
    }

    public Map<String, Boolean> getTicketOptions(String guildId) {
        Map<String, Boolean> ticketOptions = new HashMap<>();
        ServerData serverData = getServerData(guildId);

        ticketOptions.put("support", serverData.isSupportTicketActive());
        ticketOptions.put("application", serverData.isApplicationTicketActive());
        ticketOptions.put("report", serverData.isReportTicketActive());

        return ticketOptions;
    }

    public boolean isBirthdayActive(String guildId) {
        return getServerData(guildId).isBirthdayActive();
    }

    public void setBirthdayActive(String guildId, boolean isActive) {
        updateServerData(guildId, "birthday_active", isActive);
    }

    public String getBirthdayChannelId(String guildId) {
        return getServerData(guildId).getBirthdayChannelId();
    }

    public void setBirthdayChannelId(String guildId, String channelId) {
        updateServerData(guildId, "birthday_channel_ID", channelId);
    }

    public void setTicketChannel(String guildId, String channelId) {
        updateServerData(guildId, "ticket_channel_ID", channelId);
    }

    public String getTicketChannel(String guildId) {
        return getServerData(guildId).getTicketChannelId();
    }

    public void setTicketsActive(String guildId, boolean isActive) {
        updateServerData(guildId, "tickets_active", isActive);
    }

    public boolean isTicketsActive(String guildId) {
        return getServerData(guildId).isTicketsActive();
    }

    public void deleteOldTicketEmbed(String guildId, TextChannel channel) {
        String messageId = getTicketEmbedMessageId(guildId);
        if (messageId != null && !messageId.isEmpty()) {
            channel.deleteMessageById(messageId).queue(
                    success -> {
                        System.out.println("Old ticket embed deleted successfully.");
                        setTicketEmbedMessageId(guildId, null);
                    },
                    failure -> {
                        System.err.println("Failed to delete old ticket embed: " + failure.getMessage());
                        if (failure instanceof ErrorResponseException) {
                            ErrorResponseException ex = (ErrorResponseException) failure;
                            if (ex.getErrorCode() == 10008) {
                                setTicketEmbedMessageId(guildId, null);
                            }
                        }
                    }
            );
        } else {
            System.out.println("No old ticket embed message ID found or it's already null.");
        }
    }

    private void updateServerData(String guildId, String column, Object value) {
        String query = "UPDATE server_data SET " + column + " = ? WHERE Server_ID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, value);
            stmt.setString(2, guildId);
            stmt.executeUpdate();
            notifyListeners(guildId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ServerData getServerData(String guildId) {
        ServerData serverData = new ServerData();
        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "SELECT welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, " +
                    "ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, " +
                    "support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, " +
                    "ticket_channel_ID, tickets_active, TicTacToe_is_active, TicTacToe_Player1_ID, TicTacToe_Player2_ID, " +
                    "userlog_active, voice_channel_log_active, Channel_Log_active, ModLog_active, RoleLog_active, server_log_active, message_log_active, birthday_active, birthday_channel_ID " +
                    "FROM server_data WHERE Server_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, guildId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        serverData.setWelcomeChannelId(rs.getString("welcome_channel_ID"));
                        serverData.setLeaveChannelId(rs.getString("leave_channel_ID"));
                        serverData.setWelcomeActive(rs.getBoolean("welcome_active"));
                        serverData.setLeaveActive(rs.getBoolean("leave_active"));
                        serverData.setTicketCategoryId(rs.getString("ticket_category_ID"));
                        serverData.setClosedTicketCategoryId(rs.getString("closed_ticket_category_ID"));
                        serverData.setModRoleId(rs.getString("mod_role_ID"));
                        serverData.setMessageLogChannelId(rs.getString("message_log_channel_ID"));
                        serverData.setSupportTicketActive(rs.getBoolean("support_ticket_active"));
                        serverData.setApplicationTicketActive(rs.getBoolean("application_ticket_active"));
                        serverData.setReportTicketActive(rs.getBoolean("report_ticket_active"));
                        serverData.setTicketEmbedMessageId(rs.getString("ticketembed_message_id"));
                        serverData.setTicketChannelId(rs.getString("ticket_channel_ID"));
                        serverData.setTicketsActive(rs.getBoolean("tickets_active"));
                        serverData.setTicTacToeActive(rs.getBoolean("TicTacToe_is_active"));
                        serverData.setTicTacToePlayers(rs.getString("TicTacToe_Player1_ID"), rs.getString("TicTacToe_Player2_ID"));
                        serverData.setUserLogActive(rs.getBoolean("userlog_active"));
                        serverData.setVoiceChannelLogActive(rs.getBoolean("voice_channel_log_active"));
                        serverData.setChannelLogActive(rs.getBoolean("Channel_Log_active"));
                        serverData.setModLogActive(rs.getBoolean("ModLog_active"));
                        serverData.setRoleLogActive(rs.getBoolean("RoleLog_active"));
                        serverData.setServerLogActive(rs.getBoolean("server_log_active"));
                        serverData.setMessageLogActive(rs.getBoolean("message_log_active"));
                        serverData.setBirthdayActive(rs.getBoolean("birthday_active"));
                        serverData.setBirthdayChannelId(rs.getString("birthday_channel_ID"));
                    } else {
                        System.err.println("No data found for guild ID: " + guildId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error while retrieving server data for guild ID: " + guildId);
            e.printStackTrace();
        }
        return serverData;
    }

    public boolean isTicTacToeActive(String serverId) {
        String query = "SELECT TicTacToe_is_active FROM server_data WHERE Server_ID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("TicTacToe_is_active");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setTicTacToeActive(String serverId, boolean isActive) {
        String query = "UPDATE server_data SET TicTacToe_is_active = ? WHERE Server_ID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setString(2, serverId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTicTacToePlayers(String serverId, String player1Id, String player2Id) {
        String query = "UPDATE server_data SET TicTacToe_Player1_ID = ?, TicTacToe_Player2_ID = ? WHERE Server_ID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player1Id);
            stmt.setString(2, player2Id);
            stmt.setString(3, serverId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getTicTacToePlayers(String serverId) {
        String query = "SELECT TicTacToe_Player1_ID, TicTacToe_Player2_ID FROM server_data WHERE Server_ID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("TicTacToe_Player1_ID"), rs.getString("TicTacToe_Player2_ID")};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{null, null};
    }

    public void setVoiceChannelLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "voice_channel_log_active", isActive);
    }

    public boolean isVoiceChannelLogActive(String guildId) {
        return getServerData(guildId).isVoiceChannelLogActive();
    }

    public void setChannelLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "Channel_Log_active", isActive);
    }

    public boolean isChannelLogActive(String guildId) {
        return getServerData(guildId).isChannelLogActive();
    }

    public void setUserLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "userlog_active", isActive);
    }

    public boolean isUserLogActive(String guildId) {
        return getServerData(guildId).isUserLogActive();
    }

    public void setMessageLogActive(String guildId, boolean isActive) {
        updateServerData(guildId, "message_log_active", isActive);
    }

    public boolean isMessageLogActive(String guildId) {
        return getServerData(guildId).isMessageLogActive();
    }

    private static class ServerData {
        private String welcomeChannelId;
        private String leaveChannelId;
        private boolean welcomeActive;
        private boolean leaveActive;
        private String ticketCategoryId;
        private String closedTicketCategoryId;
        private String modRoleId;
        private String messageLogChannelId;
        private String voiceLogChannelId;
        private boolean supportTicketActive;
        private boolean applicationTicketActive;
        private String ticketEmbedMessageId;
        private boolean reportTicketActive;
        private String ticketChannelId;
        private boolean ticketsActive;
        private boolean ticTacToeActive;
        private String player1Id;
        private String player2Id;
        private boolean userLogActive;
        private boolean voiceChannelLogActive;
        private boolean channelLogActive;
        private boolean modLogActive;
        private boolean roleLogActive;
        private boolean serverLogActive;
        private boolean messageLogActive;
        private boolean birthdayActive;
        private String birthdayChannelId;


        public boolean isUserLogActive() { return userLogActive;}
        public boolean isVoiceChannelLogActive() {return voiceChannelLogActive;}
        public boolean isTicTacToeActive() { return ticTacToeActive;}
        public boolean isChannelLogActive() { return channelLogActive; }
        public boolean isModLogActive() { return modLogActive; }
        public boolean isRoleLogActive() { return roleLogActive; }
        public boolean isServerLogActive() { return serverLogActive; }
        public boolean isMessageLogActive() { return messageLogActive; }
        public boolean isBirthdayActive() { return birthdayActive; }

        public void setBirthdayActive(boolean birthdayActive) {
            this.birthdayActive = birthdayActive;
        }

        public String getBirthdayChannelId() {
            return birthdayChannelId;
        }

        public void setBirthdayChannelId(String birthdayChannelId) {
            this.birthdayChannelId = birthdayChannelId;
        }

        public void setServerLogActive(boolean serverLogActive) {
            this.serverLogActive = serverLogActive;
        }

        public void setRoleLogActive(boolean roleLogActive) {
            this.roleLogActive = roleLogActive;
        }
        public void setUserLogActive(boolean userLogActive) { this.userLogActive = userLogActive;}

        public void setModLogActive(boolean modLogActive) { this.modLogActive = modLogActive; }

        public void setChannelLogActive(boolean channelLogActive) {
            this.channelLogActive = channelLogActive;
        }

        public void setVoiceChannelLogActive(boolean voiceChannelLogActive) {
            this.voiceChannelLogActive = voiceChannelLogActive;
        }

        public void setMessageLogActive(boolean messageLogActive) {
            this.messageLogActive = messageLogActive;
        }

        public void setTicTacToeActive(boolean ticTacToeActive) {
            this.ticTacToeActive = ticTacToeActive;
        }

        public String[] getTicTacToePlayers() {
            return new String[]{player1Id, player2Id};
        }

        public void setTicTacToePlayers(String player1Id, String player2Id) {
            this.player1Id = player1Id;
            this.player2Id = player2Id;
        }

        public String getTicketEmbedMessageId() {
            return ticketEmbedMessageId;
        }

        public void setTicketEmbedMessageId(String ticketEmbedMessageId) {
            this.ticketEmbedMessageId = ticketEmbedMessageId;
        }

        public String getWelcomeChannelId() {
            return welcomeChannelId;
        }

        public void setWelcomeChannelId(String welcomeChannelId) {
            this.welcomeChannelId = welcomeChannelId;
        }

        public String getLeaveChannelId() {
            return leaveChannelId;
        }

        public void setLeaveChannelId(String leaveChannelId) {
            this.leaveChannelId = leaveChannelId;
        }

        public boolean isWelcomeActive() {
            return welcomeActive;
        }

        public void setWelcomeActive(boolean welcomeActive) {
            this.welcomeActive = welcomeActive;
        }

        public boolean isLeaveActive() {
            return leaveActive;
        }

        public void setLeaveActive(boolean leaveActive) {
            this.leaveActive = leaveActive;
        }

        public String getTicketCategoryId() {
            return ticketCategoryId;
        }

        public void setTicketCategoryId(String ticketCategoryId) {
            this.ticketCategoryId = ticketCategoryId;
        }

        public String getClosedTicketCategoryId() {
            return closedTicketCategoryId;
        }

        public void setClosedTicketCategoryId(String closedTicketCategoryId) {
            this.closedTicketCategoryId = closedTicketCategoryId;
        }

        public String getModRoleId() {
            return modRoleId;
        }

        public void setModRoleId(String modRoleId) {
            this.modRoleId = modRoleId;
        }

        public String getMessageLogChannelId() {
            return messageLogChannelId;
        }

        public void setMessageLogChannelId(String messageLogChannelId) {
            this.messageLogChannelId = messageLogChannelId;
        }

        public String getVoiceLogChannelId() {
            return voiceLogChannelId;
        }

        public void setVoiceLogChannelId(String voiceLogChannelId) {
            this.voiceLogChannelId = voiceLogChannelId;
        }

        public boolean isSupportTicketActive() {
            return supportTicketActive;
        }

        public void setSupportTicketActive(boolean supportTicketActive) {
            this.supportTicketActive = supportTicketActive;
        }

        public boolean isApplicationTicketActive() {
            return applicationTicketActive;
        }

        public void setApplicationTicketActive(boolean applicationTicketActive) {
            this.applicationTicketActive = applicationTicketActive;
        }

        public boolean isReportTicketActive() {
            return reportTicketActive;
        }

        public void setReportTicketActive(boolean reportTicketActive) {
            this.reportTicketActive = reportTicketActive;
        }

        public String getTicketChannelId() {
            return ticketChannelId;
        }

        public void setTicketChannelId(String ticketChannelId) {
            this.ticketChannelId = ticketChannelId;
        }

        public boolean isTicketsActive() {
            return ticketsActive;
        }

        public void setTicketsActive(boolean ticketsActive) {
            this.ticketsActive = ticketsActive;
        }
    }

    public List<TwitchLink> getTwitchLinks() {
        String query = "SELECT guild_id, twitch_username, discord_channel_id FROM twitch_links";
        List<TwitchLink> links = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                links.add(new TwitchLink(
                        rs.getString("guild_id"),
                        rs.getString("twitch_username"),
                        rs.getString("discord_channel_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return links;
    }

    public void linkTwitchToDiscord(String guildId, String twitchUsername, String discordChannelId) {
        String query = "INSERT INTO twitch_links (guild_id, twitch_username, discord_channel_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE twitch_username=?, discord_channel_id=?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, guildId);
            stmt.setString(2, twitchUsername);
            stmt.setString(3, discordChannelId);
            stmt.setString(4, twitchUsername);
            stmt.setString(5, discordChannelId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unlinkTwitchFromDiscord(String guildId) {
        String query = "DELETE FROM twitch_links WHERE guild_id=?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, guildId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public interface DataChangeListener {
        void onDataChanged(String guildId);
    }

    public class TwitchLink {
        private String guildId;
        private String twitchUsername;
        private String discordChannelId;

        public TwitchLink(String guildId, String twitchUsername, String discordChannelId) {
            this.guildId = guildId;
            this.twitchUsername = twitchUsername;
            this.discordChannelId = discordChannelId;
        }

        public String getGuildId() {
            return guildId;
        }

        public String getTwitchUsername() {
            return twitchUsername;
        }

        public String getDiscordChannelId() {
            return discordChannelId;
        }
    }
}

