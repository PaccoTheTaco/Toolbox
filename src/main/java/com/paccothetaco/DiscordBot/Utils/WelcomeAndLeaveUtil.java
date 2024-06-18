package com.paccothetaco.DiscordBot.Utils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WelcomeAndLeaveUtil extends ListenerAdapter {
    private final String dataFilePath = "serverData.txt";
    private Map<String, String> welcomeChannels = new HashMap<>();
    private Map<String, String> leaveChannels = new HashMap<>();
    private Map<String, Boolean> welcomeActive = new HashMap<>();
    private Map<String, Boolean> leaveActive = new HashMap<>();

    public WelcomeAndLeaveUtil() {
        loadChannelData();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        if (event.getName().equals("setwelcomechannel")) {
            String channelId = event.getOption("channel").getAsString();
            welcomeChannels.put(guildId, channelId);
            saveChannelData();
            event.reply("Welcome channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("setleavechannel")) {
            String channelId = event.getOption("channel").getAsString();
            leaveChannels.put(guildId, channelId);
            saveChannelData();
            event.reply("Leave channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("deactivatewelcome")) {
            welcomeActive.put(guildId, false);
            saveChannelData();
            event.reply("Welcome messages deactivated for this server.").queue();
        } else if (event.getName().equals("deactivateleave")) {
            leaveActive.put(guildId, false);
            saveChannelData();
            event.reply("Leave messages deactivated for this server.").queue();
        }
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
}
