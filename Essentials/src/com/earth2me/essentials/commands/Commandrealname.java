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
        final String whois = args[0].toLowerCase();
        for (Player p : server.getOnlinePlayers()) {
            final User u = ess.getUser(p);
            if (u.isHidden()) {
                continue;
            }
            final String displayName = ChatColor.stripColor(u.getDisplayName()).toLowerCase();
            if (!whois.equals(displayName)
                    && !displayName.equals(ChatColor.stripColor(ess.getSettings().getNicknamePrefix()) + whois)
                    && !whois.equalsIgnoreCase(u.getName())) {
                continue;
            }
            user.sendMessage(u.getDisplayName() + " " + Util.i18n("is") + " " + u.getName());
        }
    }
}
