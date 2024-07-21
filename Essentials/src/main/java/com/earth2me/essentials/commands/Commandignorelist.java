package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;

import java.util.List;


public class Commandignorelist extends EssentialsCommand {

    public Commandignorelist() {
        super("ignorelist");
    }

    @Override
    protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        List<String> ignorelist = user.getIgnoredPlayers();

        user.sendMessage(Util.format("ignoreList", Util.joinList(ignorelist)));
    }


}
