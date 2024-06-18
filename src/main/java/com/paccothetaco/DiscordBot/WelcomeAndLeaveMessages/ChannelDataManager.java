package com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ChannelDataManager {
    private final String dataFilePath = "serverData.txt";
    private Map<String, String> welcomeChannels = new HashMap<>();
    private Map<String, String> leaveChannels = new HashMap<>();
    private Map<String, Boolean> welcomeActive = new HashMap<>();
    private Map<String, Boolean> leaveActive = new HashMap<>();

    public ChannelDataManager() {
        loadChannelData();
    }

    public void setWelcomeChannel(String guildId, String channelId) {
        welcomeChannels.put(guildId, channelId);
        saveChannelData();
    }

    public void setLeaveChannel(String guildId, String channelId) {
        leaveChannels.put(guildId, channelId);
        saveChannelData();
    }

    public void setWelcomeActive(String guildId, boolean isActive) {
        welcomeActive.put(guildId, isActive);
        saveChannelData();
    }

    public void setLeaveActive(String guildId, boolean isActive) {
        leaveActive.put(guildId, isActive);
        saveChannelData();
    }

    public String getWelcomeChannelId(String guildId) {
        return welcomeChannels.get(guildId);
    }

    public String getLeaveChannelId(String guildId) {
        return leaveChannels.get(guildId);
    }

    public boolean isWelcomeActive(String guildId) {
        return welcomeActive.getOrDefault(guildId, true);
    }

    public boolean isLeaveActive(String guildId) {
        return leaveActive.getOrDefault(guildId, true);
    }

    private void loadChannelData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 5) {
                    welcomeChannels.put(parts[0], parts[1]);
                    leaveChannels.put(parts[0], parts[2]);
                    welcomeActive.put(parts[0], Boolean.parseBoolean(parts[3]));
                    leaveActive.put(parts[0], Boolean.parseBoolean(parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveChannelData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFilePath))) {
            for (String guildId : welcomeChannels.keySet()) {
                String welcomeChannelId = welcomeChannels.get(guildId);
                String leaveChannelId = leaveChannels.get(guildId);
                boolean isWelcomeActive = welcomeActive.getOrDefault(guildId, true);
                boolean isLeaveActive = leaveActive.getOrDefault(guildId, true);
                writer.write(guildId + " " + welcomeChannelId + " " + leaveChannelId + " " + isWelcomeActive + " " + isLeaveActive + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
