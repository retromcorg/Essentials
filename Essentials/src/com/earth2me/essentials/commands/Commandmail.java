package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;


public class Commandmail extends EssentialsCommand {
    public Commandmail() {
        super("mail");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
            List<String> mail = user.getMails();
            if (mail.isEmpty()) {
                throw new Exception(Util.i18n("noMail"));
            }
            for (String s : mail) {
                user.sendMessage(s);
            }
            throw new Exception(Util.i18n("mailClear"));
        }
        if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
            if (!user.isAuthorized("essentials.mail.send")) {
                throw new Exception(Util.i18n("noMailSendPerm"));
            }

            Player player = server.getPlayer(args[1]);
            User u;
            if (player != null) {
                u = ess.getUser(player);
            } else {
                u = ess.getOfflineUser(args[1]);
            }
            if (u == null) {
                throw new Exception(Util.format("playerNeverOnServer", args[1]));
            }
            if (!u.isIgnoredPlayer(user.getName())) {
                u.addMail(ChatColor.stripColor(user.getDisplayName()) + ": " + getFinalArg(args, 2));
            }
            user.sendMessage(Util.i18n("mailSent"));
            return;
        }
        if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
            user.setMails(null);
            throw new Exception(Util.i18n("mailCleared"));
        }
        throw new NotEnoughArgumentsException();
    }
}
