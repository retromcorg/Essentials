package com.earth2me.essentials;

import org.bukkit.command.CommandSender;

public interface IReplyTo {
    CommandSender getReplyTo();

    void setReplyTo(CommandSender user);
}
