package com.earth2me.essentials.commands;

import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;


public class Commandunbanip extends EssentialsCommand {
    public Commandunbanip() {
        super("unbanip");
    }

    @Override
    public void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        ess.getBans().unbanByIp(args[0]);
        sender.sendMessage(Util.i18n("unbannedIP"));
    }
}
