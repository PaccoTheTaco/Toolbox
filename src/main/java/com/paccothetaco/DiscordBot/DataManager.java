package com.paccothetaco.DiscordBot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private final String dataFilePath = "src/main/java/com/paccothetaco/DiscordBot/serverData.json";
    private Map<String, ServerData> serverDataMap = new HashMap<>();
    private List<DataChangeListener> listeners = new ArrayList<>();

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

    public void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(String guildId) {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(guildId);
        }
    }

    public void setWelcomeChannel(String guildId, String channelId) {
        System.out.println("Setting welcome channel to " + channelId);
        getServerData(guildId).setWelcomeChannelId(channelId);
        saveChannelData();
        notifyListeners(guildId);
    }

    public void setLeaveChannel(String guildId, String channelId) {
        getServerData(guildId).setLeaveChannelId(channelId);
        saveChannelData();
        notifyListeners(guildId);
    }

    public void setWelcomeActive(String guildId, boolean isActive) {
        System.out.println("Setting welcome active to " + isActive);
        getServerData(guildId).setWelcomeActive(isActive);
        saveChannelData();
        notifyListeners(guildId);
    }

    public void setLeaveActive(String guildId, boolean isActive) {
        getServerData(guildId).setLeaveActive(isActive);
        saveChannelData();
        notifyListeners(guildId);
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
        notifyListeners(guildId);
    }

    public String getTicketCategory(String guildId) {
        return getServerData(guildId).getTicketCategoryId();
    }

    public void setClosedTicketCategory(String guildId, String categoryId) {
        getServerData(guildId).setClosedTicketCategoryId(categoryId);
        saveChannelData();
        notifyListeners(guildId);
    }

    public String getClosedTicketCategory(String guildId) {
        return getServerData(guildId).getClosedTicketCategoryId();
    }

    public void setModRole(String guildId, String roleId) {
        getServerData(guildId).setModRoleId(roleId);
        saveChannelData();
        notifyListeners(guildId);
    }

    public String getModRole(String guildId) {
        return getServerData(guildId).getModRoleId();
    }

    public void setMessageLogChannel(String guildId, String channelId) {
        getServerData(guildId).setMessageLogChannelId(channelId);
        saveChannelData();
        notifyListeners(guildId);
    }

    public void deactivateMessageLog(String guildId) {
        getServerData(guildId).setMessageLogChannelId(null);
        saveChannelData();
        notifyListeners(guildId);
    }

    public String getMessageLogChannel(String guildId) {
        return getServerData(guildId).getMessageLogChannelId();
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

    public void setTicketOption(String guildId, String option, boolean active) {
        ServerData data = getServerData(guildId);
        data.setTicketOption(option, active);
        saveChannelData();
    }

    public Map<String, Boolean> getTicketOptions(String guildId) {
        return getServerData(guildId).getTicketOptions();
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
        private Map<String, Boolean> ticketOptions = new HashMap<>();

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

        public void setTicketOption(String option, boolean active) {
            ticketOptions.put(option, active);
        }

        public Map<String, Boolean> getTicketOptions() {
            return ticketOptions;
        }
    }

    public interface DataChangeListener {
        void onDataChanged(String guildId);
    }
}
