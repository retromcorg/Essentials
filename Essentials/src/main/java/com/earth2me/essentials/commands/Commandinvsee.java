package com.earth2me.essentials.commands;

import com.earth2me.essentials.InvseeManager;
import com.earth2me.essentials.OfflinePlayer;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;


public class Commandinvsee extends EssentialsCommand {
    public Commandinvsee() {
        super("invsee");
    }

    @Override
    protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        InvseeManager invseeManager = ess.getInvseeManager();

        if (args.length < 1) {
            if (!invseeManager.hasSession(user.getName()) && user.getSavedInventory() == null) {
                throw new NotEnoughArgumentsException();
            }
            stopViewing(user, invseeManager);
            throw new Exception(Util.i18n("invRestored"));
        }

        if (invseeManager.hasSession(user.getName())) {
            stopViewing(user, invseeManager);
        }

        // Allow offline players (true flag)
        User target = getPlayer(server, args, 0, true);
        boolean offline = target.getBase() instanceof OfflinePlayer;

        // Save viewer's real inventory
        user.setSavedInventory(user.getInventory().getContents());

        ItemStack[] targetContents;
        if (offline) {
            targetContents = target.getStoredInventory();
            if (targetContents == null) {
                targetContents = new ItemStack[36];
            }
        } else {
            targetContents = target.getInventory().getContents();
        }

        int viewerSize = user.getInventory().getContents().length;
        ItemStack[] display = Arrays.copyOf(targetContents, viewerSize);
        user.getInventory().setContents(display);

        invseeManager.startSession(user, target, offline);

        if (offline) {
            user.sendMessage(Util.format("invSeeOffline", target.getDisplayName()));
        } else {
            user.sendMessage(Util.format("invSee", target.getDisplayName()));
            user.sendMessage(Util.i18n("invSeeHelp"));
        }
    }

    private void stopViewing(User user, InvseeManager invseeManager) {
        invseeManager.endSession(user);
        if (user.getSavedInventory() != null) {
            user.getInventory().setContents(user.getSavedInventory());
            user.setSavedInventory(null);
        }
    }
}
