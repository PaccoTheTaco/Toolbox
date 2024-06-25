package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.CommandUtil;
import com.paccothetaco.DiscordBot.Utils.SecretUtil;
import com.paccothetaco.DiscordBot.Logsystem.LogListener;
import com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages.WelcomeAndLeave;
import com.paccothetaco.DiscordBot.Ticketsystem.ButtonInteractListener;
import com.paccothetaco.DiscordBot.Ticketsystem.SelectMenuInteractListener;
import com.paccothetaco.DiscordBot.Website.Website;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

        try {
            Website.startServer(jda);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startBot() {
        DataManager dataManager = new DataManager();
        CommandUtil commandUtil = new CommandUtil(dataManager);

        try {
            jda = JDABuilder.createDefault(SecretUtil.getToken())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .setActivity(Activity.watching("Pacco_the_Taco's Discord"))
                    .addEventListeners(
                            new ListenerAdapter() {
                                @Override
                                public void onGuildJoin(GuildJoinEvent event) {
                                    dataManager.addServerOnJoin(event.getGuild());
                                }
                            },
                            commandUtil,
                            new WelcomeAndLeave(dataManager),
                            new ButtonInteractListener(dataManager),
                            new SelectMenuInteractListener(dataManager),
                            new LogListener(dataManager)
                    )
                    .build()
                    .awaitReady();

            dataManager.checkAndAddServerIDs(jda);

            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(
                    Commands.slash("settings", "Open settings page"),
                    Commands.slash("verify", "Verify admin status")
                            .addOptions(new OptionData(OptionType.STRING, "sessionkey", "The session key to verify", true)),
                    Commands.slash("tictactoe", "Start a new Tic-Tac-Toe game"),
                    Commands.slash("move", "Make a move in Tic-Tac-Toe")
                            .addOptions(
                                    new OptionData(OptionType.INTEGER, "row", "The row to place your mark", true),
                                    new OptionData(OptionType.INTEGER, "column", "The column to place your mark", true)),
                    Commands.slash("stopgame", "Stop the current Tic-Tac-Toe game")
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
