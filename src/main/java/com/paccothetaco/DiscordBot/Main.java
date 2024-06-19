package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.CommandUtil;
import com.paccothetaco.DiscordBot.Utils.SecretUtil;
import com.paccothetaco.DiscordBot.Logsystem.LogListener;
import com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages.WelcomeAndLeave;
import com.paccothetaco.DiscordBot.Ticketsystem.ButtonInteractListener;
import com.paccothetaco.DiscordBot.Ticketsystem.SelectMenuInteractListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static JDA jda;

    public static void main(String[] args) {
        startBot();
        setupActivityTimer();
    }

    public static void startBot() {
        DataManager dataManager = new DataManager();
        CommandUtil commandUtil = new CommandUtil(dataManager);

        try {
            jda = JDABuilder.createDefault(SecretUtil.getToken())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES) // Enable MESSAGE_CONTENT and GUILD_VOICE_STATES intent
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .setActivity(Activity.watching("Pacco_the_Taco's Discord"))
                    .addEventListeners(commandUtil, new WelcomeAndLeave(dataManager),
                            new ButtonInteractListener(dataManager), new SelectMenuInteractListener(dataManager),
                            new LogListener(dataManager)) // LogListener hinzufügen
                    .build()
                    .awaitReady();

            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(
                    Commands.slash("setwelcomechannel", "Set the welcome channel")
                            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as welcome channel", true)),
                    Commands.slash("setleavechannel", "Set the leave channel")
                            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as leave channel", true)),
                    Commands.slash("deactivatewelcome", "Deactivate welcome messages for this server"),
                    Commands.slash("deactivateleave", "Deactivate leave messages for this server"),
                    Commands.slash("messagelogchannel", "Set the message log channel")
                            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as message log channel", true)),
                    Commands.slash("deactivatemessagelog", "Deactivate message logging for this server"),
                    Commands.slash("setvoicechannellog", "Set the voice channel log")
                            .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as voice channel log", true)),
                    Commands.slash("deactivatevoicechannellog", "Deactivate voice channel logging for this server"),
                    Commands.slash("ticketembed", "Send the ticket embed"),
                    Commands.slash("ticketcategory", "Set the ticket category")
                            .addOptions(new OptionData(OptionType.CHANNEL, "category", "The category to set for tickets", true)),
                    Commands.slash("closedticketcategory", "Set the closed ticket category")
                            .addOptions(new OptionData(OptionType.CHANNEL, "category", "The category to set for closed tickets", true)),
                    Commands.slash("setmodrole", "Set the moderator role")
                            .addOptions(new OptionData(OptionType.ROLE, "modrole", "The role to set for moderators", true))
            ).queue(
                    success -> System.out.println("Commands updated successfully"),
                    error -> System.err.println("Failed to update commands: " + error)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupActivityTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private boolean showServerCount = true;

            @Override
            public void run() {
                int serverCount = jda.getGuilds().size();
                int memberCount = jda.getGuilds().stream().mapToInt(guild -> guild.getMemberCount()).sum();

                if (showServerCount) {
                    jda.getPresence().setActivity(Activity.watching(serverCount + " Discord servers"));
                } else {
                    jda.getPresence().setActivity(Activity.watching(memberCount + " Members"));
                }

                showServerCount = !showServerCount;
            }
        }, 0, 10000);
    }
}
