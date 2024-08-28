package com.earth2me.essentials.commands;

import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;


public class Commanddellost extends EssentialsCommand {
    public Commanddellost() {
        super("dellost");
    }

    @Override
    public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }
        ess.getLostAndFoundWarps().delWarp(args[0].toLowerCase());
        sender.sendMessage(Util.format("deleteWarp", args[0].toLowerCase()));
    }
}
