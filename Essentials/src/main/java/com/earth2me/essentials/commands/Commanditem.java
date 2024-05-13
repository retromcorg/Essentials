package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.johnymuffin.essentials.ESSAdv;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;


public class Commanditem extends EssentialsCommand {
    public Commanditem() {
        super("item");
    }

    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }
        ItemStack stack = ess.getItemDb().get(args[0]);

        String itemname = stack.getType().toString().toLowerCase().replace("_", "");
        if (ess.getSettings().permissionBasedItemSpawn()
                ? (!user.isAuthorized("essentials.itemspawn.item-all")
                && !user.isAuthorized("essentials.itemspawn.item-" + itemname)
                && !user.isAuthorized("essentials.itemspawn.item-" + stack.getTypeId()))
                : (!user.isAuthorized("essentials.itemspawn.exempt")
                && !user.canSpawnItem(stack.getTypeId()))) {
            throw new Exception(Util.format("cantSpawnItem", itemname));
        }

        if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
            stack.setAmount(Integer.parseInt(args[1]));
        }

        if (stack.getType() == Material.AIR) {
            throw new Exception(Util.format("cantSpawnItem", "Air"));
        }

        String itemName = stack.getType().toString().toLowerCase().replace('_', ' ');
        user.sendMessage(Util.format("itemSpawn", stack.getAmount(), itemName));
        user.getInventory().addItem(stack);
        user.updateInventory();

        try {
            String issuer = (user instanceof org.bukkit.entity.Player) ? user.getName() : "Console";
            String message = String.valueOf(issuer + " Has given thyself " + stack.getAmount() + " " + itemName);
            ESSAdv.sendDiscordEmbed("Essentials - Item Command", message, "Essentials Log By Rhys B");
        } catch (Exception e) {
            user.sendMessage(e + ": " + e.getMessage());
        }


    }
}
