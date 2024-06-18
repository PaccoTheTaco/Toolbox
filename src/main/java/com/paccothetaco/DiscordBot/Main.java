package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.CommandUtil;
import com.paccothetaco.DiscordBot.Utils.SecretUtil;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Main {
    public static void main(String[] args) {
        String token = SecretUtil.getToken();

        CommandUtil commandUtil = new CommandUtil();

        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS);
        builder.setActivity(Activity.watching("for new members"));
        builder.addEventListeners(commandUtil, new WelcomeAndLeave(commandUtil));

        try {
            builder.build().updateCommands()
                    .addCommands(
                            Commands.slash("setwelcomechannel", "Set the welcome channel")
                                    .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as welcome channel", true)),
                            Commands.slash("setleavechannel", "Set the leave channel")
                                    .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as leave channel", true))
                    ).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
