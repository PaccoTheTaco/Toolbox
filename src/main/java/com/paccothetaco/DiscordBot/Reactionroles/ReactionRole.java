package com.paccothetaco.DiscordBot.Reactionroles;

public class ReactionRole {
    private String guildId;
    private String messageId;
    private String roleId;
    private String emoji;

    public ReactionRole(String guildId, String messageId, String roleId, String emoji) {
        this.guildId = guildId;
        this.messageId = messageId;
        this.roleId = roleId;
        this.emoji = emoji;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getEmoji() {
        return emoji;
    }
}
