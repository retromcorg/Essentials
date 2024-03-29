package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.InventoryWorkaround;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.minecraft.server.InventoryPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.ItemStack;


public class SignFree extends EssentialsSign {
    public SignFree() {
        super("Free");
    }

    @Override
    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        getItemStack(sign.getLine(1), 1, ess);
        return true;
    }

    @Override
    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        final ItemStack item = getItemStack(sign.getLine(1), 1, ess);
        item.setAmount(item.getType().getMaxStackSize() * 9 * 4);
        final CraftInventoryPlayer inv = new CraftInventoryPlayer(new InventoryPlayer(player.getHandle()));
        inv.clear();
        InventoryWorkaround.addItem(inv, true, item);
        player.showInventory(inv);
        Trade.log("Sign", "Free", "Interact", username, null, username, new Trade(item, ess), sign.getBlock().getLocation(), ess);
        return true;
    }
}
