package com.earth2me.essentials.commands;

import com.earth2me.essentials.Util;
import com.johnymuffin.essentials.ESSAdv;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;


public class Commandbanip extends EssentialsCommand {
    public Commandbanip() {
        super("banip");
    }

    @Override
    public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        ess.getBans().banByIp(args[0]);
        sender.sendMessage(Util.i18n("banIpAddress"));
        try {
            ESSAdv.sendDiscordEmbed("Essentials - Banip Command", "IP: " + args[0] + " has been banned", "Essentials Log By Rhys B");
        } catch (Exception e) {
            sender.sendMessage(e + ": " + e.getMessage());
        }
    }
}
