package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class Commandrealname extends EssentialsCommand {
    public Commandrealname() {
        super("realname");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }
        int found = 0;
        final String whois = args[0].toLowerCase();
        for (Player p : server.getOnlinePlayers()) {
            final User u = ess.getUser(p);
            if (u.isHidden()) {
                continue;
            }
            final String displayName =
                    u.getNickname() == null ? u.getName().toLowerCase() : u.getNickname().replaceAll("&([0-9a-f])", "").toLowerCase();
            if (!whois.equals(displayName) && !whois.equalsIgnoreCase(u.getName())) continue;
            user.sendMessage(u.getDisplayName() + " " + Util.i18n("is") + " " + u.getName());
            found++;
        }
        if(found == 0) user.sendMessage(ChatColor.DARK_RED + "Could not a user with the nickname " + ChatColor.DARK_BLUE + args[0]);
    }
}
