package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commandnick extends EssentialsCommand {
    public Commandnick() {
        super("nick");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        if (!ess.getSettings().changeDisplayName()) {
            throw new Exception(Util.i18n("nickDisplayName"));
        }

        if (args.length > 1) {
            if (!user.isAuthorized("essentials.nick.others")) {
                throw new Exception(Util.i18n("nickOthersPermission"));
            }

            setOthersNickname(server, user, args);
            return;
        }


        String nick = args[0];
        if ("off".equalsIgnoreCase(nick) || user.getName().equalsIgnoreCase(nick)) {
            user.setDisplayName(user.getName());
            user.setNickname(null);
            user.sendMessage(Util.i18n("nickNoMore"));
            return;
        }

        if (nick.matches("[^a-zA-Z_0-9]")) {
            throw new Exception(Util.i18n("nickNamesAlpha"));
        }

        for (Player p : server.getOnlinePlayers()) {
            if (user == p) {
                continue;
            }
            String dn = p.getDisplayName().toLowerCase();
            String n = p.getName().toLowerCase();
            String nk = nick.toLowerCase();
            if (nk.equals(dn) || nk.equals(n)) {
                throw new Exception(Util.i18n("nickInUse"));
            }
        }

        user.setDisplayName(user.getGroup() + ess.getSettings().getNicknamePrefix() + nick);
        user.setNickname(nick);
        user.sendMessage(Util.format("nickSet", user.getDisplayName() + "§7."));
    }

    @Override
    public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
        if (args.length < 2) {
            throw new NotEnoughArgumentsException();
        }

        if (!ess.getSettings().changeDisplayName()) {
            sender.sendMessage(Util.i18n("nickDisplayName"));
            return;
        }

        setOthersNickname(server, sender, args);

    }

    private void setOthersNickname(Server server, CommandSender sender, String[] args) throws Exception {
        User target = getPlayer(server, args, 0);
        String nick = args[1];
        if ("off".equalsIgnoreCase(nick) || target.getName().equalsIgnoreCase(nick)) {
            target.setDisplayName(target.getName());
            target.setNickname(null);
            target.sendMessage(Util.i18n("nickNoMore"));
        } else {
            target.setDisplayName(ess.getSettings().getNicknamePrefix() + nick);
            target.setNickname(nick);
            target.sendMessage(Util.format("nickSet", target.getDisplayName() + "§7."));
        }
        sender.sendMessage(Util.i18n("nickChanged"));
    }
}
