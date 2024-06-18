package com.paccothetaco.DiscordBot.Utils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CommandUtil extends ListenerAdapter {
    private final String dataFilePath = "serverData.txt";
    private Map<String, String> welcomeChannels = new HashMap<>();
    private Map<String, String> leaveChannels = new HashMap<>();

    public CommandUtil() {
        loadChannelData();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("setwelcomechannel")) {
            String channelId = event.getOption("channel").getAsString();
            welcomeChannels.put(event.getGuild().getId(), channelId);
            saveChannelData();
            event.reply("Welcome channel set to <#" + channelId + ">").queue();
        } else if (event.getName().equals("setleavechannel")) {
            String channelId = event.getOption("channel").getAsString();
            leaveChannels.put(event.getGuild().getId(), channelId);
            saveChannelData();
            event.reply("Leave channel set to <#" + channelId + ">").queue();
        }
    }

    private void loadChannelData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    welcomeChannels.put(parts[0], parts[1]);
                    leaveChannels.put(parts[0], parts[2]);
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
                writer.write(guildId + " " + welcomeChannelId + " " + leaveChannelId + "\n");
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
}
