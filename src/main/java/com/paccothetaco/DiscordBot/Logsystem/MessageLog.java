package com.paccothetaco.DiscordBot.Logsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import com.paccothetaco.DiscordBot.DataManager;

import java.awt.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageLog {
    private final DataManager dataManager;
    private final ConcurrentHashMap<String, String> messageCache = new ConcurrentHashMap<>();

    public MessageLog(DataManager dataManager) {
        this.dataManager = dataManager;
        loadCache();
    }

    public void setLogChannel(String guildId, String channelId) {
        dataManager.setLogChannel(guildId, channelId);
    }

    public void deactivateLog(String guildId) {
        dataManager.setLogChannel(guildId, null);
    }

    public void handleMessageUpdate(MessageUpdateEvent event) {
        String guildId = event.getGuild().getId();
        String channelId = dataManager.getLogChannel(guildId);
        if (channelId != null) {
            TextChannel logChannel = event.getGuild().getTextChannelById(channelId);
            if (logChannel != null) {
                String oldMessage = messageCache.get(event.getMessageId());
                String authorMention = "<@" + event.getAuthor().getId() + ">";

                if (oldMessage == null) {
                    oldMessage = "unknown (cache may have missed it)";
                }
                String newMessage = event.getMessage().getContentRaw();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.ORANGE);
                embed.setTitle("Message edited in #" + event.getChannel().getName());
                embed.addField("Author:", authorMention, false);
                embed.addField("Before:", oldMessage, false);
                embed.addField("After:", newMessage, false);
                embed.setFooter("ID: " + event.getMessageId(), null);
                embed.setTimestamp(event.getMessage().getTimeEdited());

                logChannel.sendMessageEmbeds(embed.build()).queue();

                messageCache.put(event.getMessageId(), newMessage); // Update the cache with the new message content
                saveCache();
            }
        }
    }

    public void handleMessageDelete(MessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        String channelId = dataManager.getLogChannel(guildId);
        if (channelId != null) {
            TextChannel logChannel = event.getGuild().getTextChannelById(channelId);
            if (logChannel != null) {
                String messageContent = messageCache.get(event.getMessageId());
                String authorMention = "<@" + messageCache.get(event.getMessageId() + "_author") + ">";

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);
                embed.setTitle("Message deleted in #" + event.getChannel().getName());
                embed.addField("Author:", authorMention, false);
                embed.setDescription(messageContent != null ? messageContent : "unknown (cache may have missed it)");
                embed.setFooter("ID: " + event.getMessageId(), null);

                logChannel.sendMessageEmbeds(embed.build()).queue();

                messageCache.remove(event.getMessageId());
                messageCache.remove(event.getMessageId() + "_author");
                saveCache();
            }
        }
    }

    public void cacheMessage(String messageId, String content, String authorId) {
        messageCache.put(messageId, content);
        messageCache.put(messageId + "_author", authorId);
        saveCache();
    }

    private void saveCache() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("messageCache.ser"))) {
            oos.writeObject(messageCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCache() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("messageCache.ser"))) {
            ConcurrentHashMap<String, String> loadedCache = (ConcurrentHashMap<String, String>) ois.readObject();
            messageCache.putAll(loadedCache);
        } catch (FileNotFoundException e) {
            // File not found, create a new cache
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
