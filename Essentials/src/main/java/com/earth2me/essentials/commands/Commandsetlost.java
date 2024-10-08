package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Location;
import org.bukkit.Server;


public class Commandsetlost extends EssentialsCommand {
    public Commandsetlost() {
        super("setlost");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        Location loc = user.getLocation();
        ess.getLostAndFoundWarps().setWarp(args[0].toLowerCase(), loc);
        user.sendMessage(Util.format("warpSet", args[0].toLowerCase()));
    }
}
