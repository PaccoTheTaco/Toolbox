package com.paccothetaco.DiscordBot.Reactionroles;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class ReactionrolesListener extends ListenerAdapter {
    private final DataManager dataManager;

    public ReactionrolesListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        List<ReactionRole> roles = dataManager.getReactionRoles(event.getMessageId());
        for (ReactionRole role : roles) {
            if (role.getEmoji().equals(event.getReaction().getEmoji().getName())) {
                event.getGuild().addRoleToMember(event.getUser(), event.getGuild().getRoleById(role.getRoleId())).queue();
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUser().isBot()) return;

        List<ReactionRole> roles = dataManager.getReactionRoles(event.getMessageId());
        for (ReactionRole role : roles) {
            if (role.getEmoji().equals(event.getReaction().getEmoji().getName())) {
                event.getGuild().removeRoleFromMember(event.getUser(), event.getGuild().getRoleById(role.getRoleId())).queue();
            }
        }
    }
}
