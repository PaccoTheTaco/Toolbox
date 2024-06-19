package com.paccothetaco.DiscordBot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private final String dataFilePath = "src/main/java/com/paccothetaco/DiscordBot/serverData.json";
    private Map<String, ServerData> serverDataMap = new HashMap<>();

    public DataManager() {
        createDataFile();
        loadChannelData();
    }

    private void createDataFile() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setWelcomeChannel(String guildId, String channelId) {
        getServerData(guildId).setWelcomeChannelId(channelId);
        saveChannelData();
    }

    public void setLeaveChannel(String guildId, String channelId) {
        getServerData(guildId).setLeaveChannelId(channelId);
        saveChannelData();
    }

    public void setWelcomeActive(String guildId, boolean isActive) {
        getServerData(guildId).setWelcomeActive(isActive);
        saveChannelData();
    }

    public void setLeaveActive(String guildId, boolean isActive) {
        getServerData(guildId).setLeaveActive(isActive);
        saveChannelData();
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
        getServerData(guildId).setTicketCategoryId(categoryId);
        saveChannelData();
    }

    public String getTicketCategory(String guildId) {
        return getServerData(guildId).getTicketCategoryId();
    }

    public void setClosedTicketCategory(String guildId, String categoryId) {
        getServerData(guildId).setClosedTicketCategoryId(categoryId);
        saveChannelData();
    }

    public String getClosedTicketCategory(String guildId) {
        return getServerData(guildId).getClosedTicketCategoryId();
    }

    public void setModRole(String guildId, String roleId) {
        getServerData(guildId).setModRoleId(roleId);
        saveChannelData();
    }

    public String getModRole(String guildId) {
        return getServerData(guildId).getModRoleId();
    }

    public void setMessageLogChannel(String guildId, String channelId) {
        getServerData(guildId).setMessageLogChannelId(channelId);
        saveChannelData();
    }

    public void deactivateMessageLog(String guildId) {
        getServerData(guildId).setMessageLogChannelId(null);
        saveChannelData();
    }

    public String getMessageLogChannel(String guildId) {
        return getServerData(guildId).getMessageLogChannelId();
    }

    public void setVoiceLogChannel(String guildId, String channelId) {
        getServerData(guildId).setVoiceLogChannelId(channelId);
        saveChannelData();
    }

    public void deactivateVoiceLog(String guildId) {
        getServerData(guildId).setVoiceLogChannelId(null);
        saveChannelData();
    }

    public String getVoiceLogChannel(String guildId) {
        return getServerData(guildId).getVoiceLogChannelId();
    }

    private ServerData getServerData(String guildId) {
        return serverDataMap.computeIfAbsent(guildId, k -> new ServerData());
    }

    private void loadChannelData() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(dataFilePath);
        if (file.exists() && file.length() > 0) { // Check if file exists and is not empty
            try {
                serverDataMap = mapper.readValue(file, new TypeReference<Map<String, ServerData>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveChannelData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(dataFilePath), serverDataMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServerData {
        private String welcomeChannelId;
        private String leaveChannelId;
        private boolean welcomeActive = true;
        private boolean leaveActive = true;
        private String ticketCategoryId;
        private String closedTicketCategoryId;
        private String modRoleId;
        private String messageLogChannelId;
        private String voiceLogChannelId;

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
    }
}
