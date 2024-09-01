package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.LostAndFoundWarps;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Commandlost extends EssentialsCommand {

    public Commandlost() {
        super("lost");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        LostAndFoundWarps lostAndFoundWarps = ess.getLostAndFoundWarps();
        if (lostAndFoundWarps.isEmpty()) {
            throw new Exception(Util.i18n("noLostWarpsDefined"));
        }

        if (args.length == 0) {
            warpUser(user, "default");
        } else {
            // Check if the first character of the first argument is alphanumeric or an underscore
            String input = args[0].toLowerCase();
            char firstChar = input.charAt(0);
            if (Character.isLetterOrDigit(firstChar) || firstChar == '_') {
                warpUser(user, String.valueOf(firstChar));
            } else {
                throw new Exception(Util.i18n("lostWarpNotExist"));
            }
        }
    }

    private void warpUser(User user, String name) throws Exception {
        Trade charge = new Trade(this.getName(), ess);
        charge.isAffordableFor(user);

        user.getTeleport().lostAndFoundWarp(name, charge);
    }
}
