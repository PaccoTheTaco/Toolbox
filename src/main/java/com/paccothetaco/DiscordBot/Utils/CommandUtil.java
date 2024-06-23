package com.paccothetaco.DiscordBot.Utils;

import com.paccothetaco.DiscordBot.Games.TicTacToe;
import com.paccothetaco.DiscordBot.Website.Website;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class CommandUtil extends ListenerAdapter {
    private final Map<String, TicTacToe> games;

    public CommandUtil() {
        this.games = new HashMap<>();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        switch (event.getName()) {
            case "settings" -> handleSettings(event, guildId);
            case "verify" -> handleVerify(event, guildId);
            case "tictactoe" -> handleTicTacToe(event);
            case "move" -> handleMove(event);
            default -> event.reply("This command doesn't exist").setEphemeral(true).queue();
        }
    }

    private void handleMove(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        TicTacToe game = games.get(userId);

        if (game == null) {
            event.reply("You don't have an active Tic-Tac-Toe game. Start a new game with `/tictactoe`.").setEphemeral(true).queue();
            return;
        }

        int row = event.getOption("row").getAsInt() - 1;
        int col = event.getOption("column").getAsInt() - 1;

        if (game.placeMark(row, col)) {
            if (game.checkForWin()) {
                event.reply("Player " + game.getCurrentPlayer() + " wins! Here is the final board:\n" + game.printBoard()).setEphemeral(true).queue();
                games.remove(userId);
            } else if (game.isBoardFull()) {
                event.reply("The game is a tie! Here is the final board:\n" + game.printBoard()).setEphemeral(true).queue();
                games.remove(userId);
            } else {
                game.changePlayer();
                event.reply("Move accepted. Here is the board:\n" + game.printBoard() +
                        "\nIt's now player " + game.getCurrentPlayer() + "'s turn.").setEphemeral(true).queue();
            }
        } else {
            event.reply("Invalid move. Try again. Here is the board:\n" + game.printBoard()).setEphemeral(true).queue();
        }
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

    private void handleTicTacToe(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        TicTacToe game = games.get(userId);

        if (game == null) {
            game = new TicTacToe();
            games.put(userId, game);
            event.reply("New Tic-Tac-Toe game started! Here is the board:\n" + game.printBoard() +
                    "\nYou are player X. Use `/move row column` to make a move.").setEphemeral(true).queue();
        } else {
            event.reply("You already have an ongoing game. Here is the board:\n" + game.printBoard() +
                    "\nUse `/move row column` to make a move.").setEphemeral(true).queue();
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
