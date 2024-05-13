package com.earth2me.essentials;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;

public final class Console implements IReplyTo {
    public final static String NAME = "Console";
    private static final Console instance = new Console();
    private CommandSender replyTo;

    private Console() {

    }

    public static CommandSender getCommandSender(Server server) throws Exception {
        if (!(server instanceof CraftServer)) {
            throw new Exception(Util.i18n("invalidServer"));
        }
        return ((CraftServer) server).getServer().console;
    }

    public static Console getConsoleReplyTo() {
        return instance;
    }

    public CommandSender getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(CommandSender user) {
        replyTo = user;
    }
}
