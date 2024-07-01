package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Logsystem.Listener.*;
import com.paccothetaco.DiscordBot.Ticketsystem.ButtonInteractListener;
import com.paccothetaco.DiscordBot.Ticketsystem.SelectMenuInteractListener;
import com.paccothetaco.DiscordBot.Utils.CommandUtil;
import com.paccothetaco.DiscordBot.Utils.SecretUtil;
import com.paccothetaco.DiscordBot.Website.Website;
import com.paccothetaco.DiscordBot.WelcomeAndLeaveMessages.WelcomeAndLeave;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
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
                    .enableIntents(
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.all(MemberCachePolicy.ALL))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES)
                    .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER)
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
                            new NameChangeListener(dataManager),
                            new NicknameChangeListener(dataManager),
                            new MessageLogListener(dataManager),
                            new VoiceLogListener(dataManager),
                            new ChannelListener(dataManager),
                            new ModLogListener(dataManager),
                            new RoleLogListener(dataManager)
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
                    Commands.slash("stopgame", "Stop the current Tic-Tac-Toe game"),
                    Commands.slash("toolboxgpt", "Ask ToolboxGPT a question")
                            .addOptions(new OptionData(OptionType.STRING, "question", "The question to ask ToolboxGPT", true))
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
