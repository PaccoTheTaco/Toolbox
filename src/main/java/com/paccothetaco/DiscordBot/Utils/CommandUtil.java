package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Games.TicTacToe;
import com.paccothetaco.DiscordBot.ToolboxGPT;
import com.paccothetaco.DiscordBot.Website.Website;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

public class CommandUtil extends ListenerAdapter {
    private DataManager dataManager;
    private TicTacToe ticTacToe;
    private Timer inactivityTimer;

    public CommandUtil(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        switch (event.getName()) {
            case "settings" -> handleSettings(event, guildId);
            case "verify" -> handleVerify(event, guildId);
            case "tictactoe" -> handleTicTacToe(event);
            case "move" -> handleMove(event);
            case "stopgame" -> handleStopGame(event);
            case "toolboxgpt" -> handleToolboxGPT(event);  // Neuer Fall hinzugefügt

        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("join_tictactoe")) {
            handleJoinTicTacToe(event);
        }
    }

    private void handleToolboxGPT(SlashCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();

        try {
            String response = ToolboxGPT.getGPTResponse(question);
            event.reply(response).queue();
        } catch (IOException e) {
            event.reply("Sorry, there was an error processing your request.").setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    private String getMemberEffectiveName(String memberId, SlashCommandInteractionEvent event) {
        Member member = event.getGuild().retrieveMemberById(memberId).complete();
        if (member != null) {
            return member.getEffectiveName();
        } else {
            User user = event.getJDA().retrieveUserById(memberId).complete();
            if (user != null) {
                return user.getName();
            } else {
                System.out.println("Error: Could not find member or user with ID " + memberId);
                return "Unknown User";
            }
        }
    }

    private String getMemberEffectiveName(String memberId, ButtonInteractionEvent event) {
        Member member = event.getGuild().retrieveMemberById(memberId).complete();
        if (member != null) {
            return member.getEffectiveName();
        } else {
            User user = event.getJDA().retrieveUserById(memberId).complete();
            if (user != null) {
                return user.getName();
            } else {
                System.out.println("Error: Could not find member or user with ID " + memberId);
                return "Unknown User";
            }
        }
    }

    private void handleTicTacToe(SlashCommandInteractionEvent event) {
        String serverId = event.getGuild().getId();

        // Überprüfen, ob bereits ein Spiel aktiv ist
        if (dataManager.isTicTacToeActive(serverId)) {
            event.reply("A Tic-Tac-Toe game is already running on this server.").setEphemeral(true).queue();
            return;
        }

        String player1Id = event.getUser().getId();
        ticTacToe = new TicTacToe(player1Id);
        dataManager.setTicTacToePlayers(serverId, player1Id, null);
        dataManager.setTicTacToeActive(serverId, true);
        startInactivityTimer(serverId);

        String player1Name = getMemberEffectiveName(player1Id, event);
        event.reply("Tic-Tac-Toe game started! You are player 1 (X). Waiting for a second player.")
                .addActionRow(Button.primary("join_tictactoe", "Join the game")).queue();

        System.out.println("Player 1: " + player1Name + " (ID: " + player1Id + ")");
    }


    private void handleJoinTicTacToe(ButtonInteractionEvent event) {
        String serverId = event.getGuild().getId();
        String player2Id = event.getUser().getId();

        // Überprüfen, ob das Spiel aktiv ist
        if (!dataManager.isTicTacToeActive(serverId)) {
            event.reply("No Tic-Tac-Toe game is running on this server.").setEphemeral(true).queue();
            return;
        }

        // Spieler 1 ID abrufen
        String player1Id = ticTacToe.getPlayer1Id();

        // Überprüfen, ob Spieler 1 versucht, sich selbst beizutreten
        if (player2Id.equals(player1Id)) {
            event.reply("You cannot join your own game.").setEphemeral(true).queue();
            return;
        }

        // Überprüfen, ob das Spiel bereits einen zweiten Spieler hat
        if (ticTacToe.getPlayer2Id() != null) {
            event.reply("A second player has already joined this game.").setEphemeral(true).queue();
            return;
        }

        // Spieler 2 setzen und das Spiel starten
        ticTacToe.setPlayer2Id(player2Id);
        dataManager.setTicTacToePlayers(serverId, player1Id, player2Id);
        resetInactivityTimer(serverId);

        String player1Name = getMemberEffectiveName(player1Id, event);
        String player2Name = getMemberEffectiveName(player2Id, event);

        event.reply(player1Name + " (X) vs " + player2Name + " (O)\nThe game can begin, here is your board:\n" +
                ticTacToe.printBoard() + "\n" + player1Name + ", it's your turn. Use /move row column to place your X.").queue();

        System.out.println("Player 2: " + player2Name + " (ID: " + player2Id + ")");
    }


    private void handleMove(SlashCommandInteractionEvent event) {
        String serverId = event.getGuild().getId();
        if (!dataManager.isTicTacToeActive(serverId)) {
            event.reply("No Tic-Tac-Toe game is running on this server.").setEphemeral(true).queue();
            return;
        }

        String currentUserId = event.getUser().getId();
        if (!currentUserId.equals(ticTacToe.getPlayer1Id()) && !currentUserId.equals(ticTacToe.getPlayer2Id())) {
            event.reply("You are not a player in this Tic-Tac-Toe game.").setEphemeral(true).queue();
            return;
        }

        if (!currentUserId.equals(ticTacToe.getCurrentPlayerId())) {
            event.reply("It's not your turn.").setEphemeral(true).queue();
            return;
        }

        int row = event.getOption("row").getAsInt();
        int column = event.getOption("column").getAsInt();

        if (ticTacToe.placeMark(row, column)) {
            resetInactivityTimer(serverId);

            String currentPlayerName = getMemberEffectiveName(ticTacToe.getCurrentPlayerId(), event);
            String nextPlayerName = getMemberEffectiveName(ticTacToe.getNextPlayerId(), event);

            event.reply("Board:\n" + ticTacToe.printBoard() + "\n" + nextPlayerName + ", it's your turn. Use /move row column to place your " + ticTacToe.getCurrentPlayer() + ".").queue();

            if (ticTacToe.checkForWin()) {
                dataManager.setTicTacToeActive(serverId, false);
                event.getChannel().sendMessage("Player " + currentPlayerName + " has won!").queue();
            } else if (ticTacToe.isBoardFull()) {
                dataManager.setTicTacToeActive(serverId, false);
                event.getChannel().sendMessage("The game ends in a draw!").queue();
            }

            ticTacToe.changePlayer();
        } else {
            event.reply("Invalid move. Try again.").queue();
        }
    }

    private void handleStopGame(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to stop this game.").setEphemeral(true).queue();
            return;
        }

        String serverId = event.getGuild().getId();
        if (dataManager.isTicTacToeActive(serverId)) {
            dataManager.setTicTacToeActive(serverId, false);
            event.reply("The Tic-Tac-Toe game has been stopped.").setEphemeral(true).queue();
        } else {
            event.reply("No Tic-Tac-Toe game is running on this server.").setEphemeral(true).queue();
        }
    }

    private void startInactivityTimer(String serverId) {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
        inactivityTimer = new Timer();
        inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dataManager.setTicTacToeActive(serverId, false);
            }
        }, 5 * 60 * 1000);
    }

    private void resetInactivityTimer(String serverId) {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
        startInactivityTimer(serverId);
    }

    private void handleSettings(SlashCommandInteractionEvent event, String guildId) {
        if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            String sessionKey = generateSessionKey(guildId);
            String settingsUrl = "https://paccothetaco.com/settings?sk=" + sessionKey;
            Website.addSessionKey(sessionKey, guildId);
            event.reply("Go to your settings: " + settingsUrl).setEphemeral(true).queue();
        } else {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
        }
    }

    private void handleVerify(SlashCommandInteractionEvent event, String guildId) {
        if (event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            String sessionKey = event.getOption("sessionkey").getAsString();
            String sessionGuildId = Website.getGuildId(sessionKey);
            if (guildId.equals(sessionGuildId)) {
                Website.addVerifiedSessionKey(sessionKey, guildId);
                event.reply("You have been verified. Redirecting to settings...")
                        .setEphemeral(true)
                        .addActionRow(Button.link("https://paccothetaco.com/settings?sk=" + sessionKey, "Go to Settings"))
                        .queue();
            } else {
                event.reply("Invalid session key for this server.").setEphemeral(true).queue();
            }
        } else {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
        }
    }

    private String generateSessionKey(String guildId) {
        SecureRandom random = new SecureRandom();
        StringBuilder sessionKey = new StringBuilder(guildId + "_");

        for (int i = 0; i < 10; i++) {
            int digit = random.nextInt(10);
            sessionKey.append(digit);
        }

        return sessionKey.toString();
    }
}
