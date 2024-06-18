package com.paccothetaco.DiscordBot;

import com.paccothetaco.DiscordBot.Utils.WelcomeAndLeaveUtil;
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

        WelcomeAndLeaveUtil welcomeAndLeaveUtil = new WelcomeAndLeaveUtil();

        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS);
        builder.setActivity(Activity.watching("for new members"));
        builder.addEventListeners(welcomeAndLeaveUtil, new WelcomeAndLeave(welcomeAndLeaveUtil));

        try {
            builder.build().updateCommands()
                    .addCommands(
                            Commands.slash("setwelcomechannel", "Set the welcome channel")
                                    .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as welcome channel", true)),
                            Commands.slash("setleavechannel", "Set the leave channel")
                                    .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as leave channel", true)),
                            Commands.slash("deactivatewelcome", "Deactivate welcome messages for this server"),
                            Commands.slash("deactivateleave", "Deactivate leave messages for this server")
                    ).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
