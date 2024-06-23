package com.paccothetaco.DiscordBot;

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
            String insertQuery = "INSERT INTO server_data (Server_ID, welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, ticket_channel_ID, tickets_active) VALUES (?, NULL, NULL, false, false, NULL, NULL, NULL, NULL, true, true, true, NULL, NULL, false)";

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

    public void addServerOnJoin(Guild guild) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectQuery = "SELECT COUNT(*) FROM server_data WHERE Server_ID = ?";
            String insertQuery = "INSERT INTO server_data (Server_ID, welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, ticket_channel_ID, tickets_active) VALUES (?, NULL, NULL, false, false, NULL, NULL, NULL, NULL, true, true, true, NULL, NULL, false)";

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

    public void setTicketOption(String guildId, String option, boolean active) {
        updateServerData(guildId, option + "_ticket_active", active);
    }

    public Map<String, Boolean> getTicketOptions(String guildId) {
        Map<String, Boolean> ticketOptions = new HashMap<>();
        ServerData serverData = getServerData(guildId);

        ticketOptions.put("support", serverData.isSupportTicketActive());
        ticketOptions.put("application", serverData.isApplicationTicketActive());
        ticketOptions.put("report", serverData.isReportTicketActive());

        return ticketOptions;
    }

    // Neue Methoden für Ticket Channel
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

    // Methode zum Löschen des alten Ticketembeds
    public void deleteOldTicketEmbed(String guildId, TextChannel channel) {
        String messageId = getTicketEmbedMessageId(guildId);
        if (messageId != null && !messageId.isEmpty()) {
            channel.deleteMessageById(messageId).queue(
                    success -> {
                        System.out.println("Old ticket embed deleted successfully.");
                        // Setze die Message ID auf null, nachdem das Embed erfolgreich gelöscht wurde
                        setTicketEmbedMessageId(guildId, null);
                    },
                    failure -> {
                        System.err.println("Failed to delete old ticket embed: " + failure.getMessage());
                        // Falls die Nachricht nicht gefunden wurde, setze die Message ID auf null, um Inkonsistenzen zu vermeiden
                        if (failure instanceof ErrorResponseException) {
                            ErrorResponseException ex = (ErrorResponseException) failure;
                            if (ex.getErrorCode() == 10008) { // Unknown Message
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
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT welcome_channel_ID, leave_channel_ID, welcome_active, leave_active, " +
                    "ticket_category_ID, closed_ticket_category_ID, mod_role_ID, message_log_channel_ID, " +
                    "support_ticket_active, application_ticket_active, report_ticket_active, ticketembed_message_id, ticket_channel_ID, tickets_active " +
                    "FROM server_data WHERE Server_ID = ?";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, guildId);
            rs = stmt.executeQuery();

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
            } else {
                System.err.println("No data found for guild ID: " + guildId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return serverData;
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

        // Getter und Setter für ticketsActive
        public boolean isTicketsActive() {
            return ticketsActive;
        }

        public void setTicketsActive(boolean ticketsActive) {
            this.ticketsActive = ticketsActive;
        }

        // Bestehende Getter und Setter
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
    }

    public interface DataChangeListener {
        void onDataChanged(String guildId);
    }
}
