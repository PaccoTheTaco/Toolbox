package com.paccothetaco.DiscordBot.Games;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class Counting extends ListenerAdapter {
    private DataManager dataManager;
    private JDA jda;
    private Map<String, String> lastUserMap = new HashMap<>();
    private Map<String, Integer> lastNumberMap = new HashMap<>();

    public Counting(DataManager dataManager, JDA jda) {
        this.dataManager = dataManager;
        this.jda = jda;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();
        String countingChannelId = dataManager.getCountingChannelID(guildId);

        if (countingChannelId == null || !countingChannelId.equals(channelId)) return;

        String messageContent = event.getMessage().getContentRaw();
        if (!messageContent.matches("\\d+")) return;

        int number = Integer.parseInt(messageContent);
        String lastUserId = lastUserMap.getOrDefault(guildId, "");
        int lastNumber = lastNumberMap.getOrDefault(guildId, 0);

        if (event.getAuthor().getId().equals(lastUserId)) {
            sendPrivateMessage(event.getAuthor(), "You can't count alone! Wait for someone else.");
            return;
        }

        if (number == lastNumber + 1) {
            lastUserMap.put(guildId, event.getAuthor().getId());
            lastNumberMap.put(guildId, number);
            dataManager.updateCountScore(guildId, event.getAuthor().getId(), number);
            dataManager.updateBestCountScore(guildId, event.getAuthor().getId(), number);

            event.getMessage().addReaction(Emoji.fromUnicode("✅")).queue();
        } else {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + " has written the wrong number! Now you have to start all over again.").queue();
            lastUserMap.put(guildId, "");
            lastNumberMap.put(guildId, 0);
            dataManager.resetCountScore(guildId, event.getAuthor().getId());
        }
    }

    private void sendPrivateMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }
}
