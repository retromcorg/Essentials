package com.earth2me.essentials.signs;

import com.earth2me.essentials.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;


public class EssentialsSign {
    private static final Set<Material> EMPTY_SET = new HashSet<Material>();
    //TODO: Add these settings to messages
    private static final String FORMAT_SUCCESS = "§1[%s]";
    private static final String FORMAT_TEMPLATE = "[%s]";
    private static final String FORMAT_FAIL = "§4[%s]";
    protected transient final String signName;

    public EssentialsSign(final String signName) {
        this.signName = signName;
    }

    public static boolean checkIfBlockBreaksSigns(final Block block) {
        final Block sign = block.getRelative(BlockFace.UP);
        if (sign.getType() == Material.SIGN_POST && isValidSign(new BlockSign(sign))) {
            return true;
        }
        final BlockFace[] directions = new BlockFace[]
                {
                        BlockFace.NORTH,
                        BlockFace.EAST,
                        BlockFace.SOUTH,
                        BlockFace.WEST
                };
        for (BlockFace blockFace : directions) {
            final Block signblock = block.getRelative(blockFace);
            if (signblock.getType() == Material.WALL_SIGN) {
                final org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) signblock.getState().getData();
                if (signMat.getFacing() == blockFace && isValidSign(new BlockSign(signblock))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidSign(final ISign sign) {
        return sign.getLine(0).matches("§1\\[.*\\]");
    }

    public final boolean onSignCreate(final SignChangeEvent event, final IEssentials ess) {
        final ISign sign = new EventSign(event);
        final User user = ess.getUser(event.getPlayer());
        if (!(user.isAuthorized("essentials.signs." + signName.toLowerCase() + ".create")
                || user.isAuthorized("essentials.signs.create." + signName.toLowerCase()))) {
            // Return true, so other plugins can use the same sign title, just hope
            // they won't change it to §1[Signname]
            return true;
        }
        sign.setLine(0, String.format(FORMAT_FAIL, this.signName));
        try {
            final boolean ret = onSignCreate(sign, user, getUsername(user), ess);
            if (ret) {
                sign.setLine(0, getSuccessName());
            }
            return ret;
        } catch (ChargeException ex) {
            ess.showError(user, ex, signName);
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
        }
        // Return true, so the player sees the wrong sign.
        return true;
    }

    public String getSuccessName() {
        return String.format(FORMAT_SUCCESS, this.signName);
    }

    public String getTemplateName() {
        return String.format(FORMAT_TEMPLATE, this.signName);
    }

    private String getUsername(final User user) {
        return user.getName().substring(0, user.getName().length() > 13 ? 13 : user.getName().length());
    }

    public final boolean onSignInteract(final Block block, final Player player, final IEssentials ess) {
        final ISign sign = new BlockSign(block);
        final User user = ess.getUser(player);
        try {
            return (user.isAuthorized("essentials.signs." + signName.toLowerCase() + ".use")
                    || user.isAuthorized("essentials.signs.use." + signName.toLowerCase()))
                    && onSignInteract(sign, user, getUsername(user), ess);
        } catch (ChargeException ex) {
            ess.showError(user, ex, signName);
            return false;
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
            return false;
        }
    }

    public final boolean onSignBreak(final Block block, final Player player, final IEssentials ess) {
        final ISign sign = new BlockSign(block);
        final User user = ess.getUser(player);
        try {
            return (user.isAuthorized("essentials.signs." + signName.toLowerCase() + ".break")
                    || user.isAuthorized("essentials.signs.break." + signName.toLowerCase()))
                    && onSignBreak(sign, user, getUsername(user), ess);
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
            return false;
        }
    }

    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        return true;
    }

    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        return true;
    }

    protected boolean onSignBreak(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        return true;
    }

    public final boolean onBlockPlace(final Block block, final Player player, final IEssentials ess) {
        User user = ess.getUser(player);
        try {
            return onBlockPlace(block, user, getUsername(user), ess);
        } catch (ChargeException ex) {
            ess.showError(user, ex, signName);
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
        }
        return false;
    }

    public final boolean onBlockInteract(final Block block, final Player player, final IEssentials ess) {
        User user = ess.getUser(player);
        try {
            return onBlockInteract(block, user, getUsername(user), ess);
        } catch (ChargeException ex) {
            ess.showError(user, ex, signName);
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
        }
        return false;
    }

    public final boolean onBlockBreak(final Block block, final Player player, final IEssentials ess) {
        User user = ess.getUser(player);
        try {
            return onBlockBreak(block, user, getUsername(user), ess);
        } catch (SignException ex) {
            ess.showError(user, ex, signName);
        }
        return false;
    }

    public boolean onBlockExplode(final Block block, final IEssentials ess) {
        return true;
    }

    public boolean onBlockBurn(final Block block, final IEssentials ess) {
        return true;
    }

    public boolean onBlockIgnite(final Block block, final IEssentials ess) {
        return true;
    }

    public boolean onBlockPush(final Block block, final IEssentials ess) {
        return true;
    }

    protected boolean onBlockPlace(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        return true;
    }

    protected boolean onBlockInteract(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        return true;
    }

    protected boolean onBlockBreak(final Block block, final User player, final String username, final IEssentials ess) throws SignException {
        return true;
    }

    public Set<Material> getBlocks() {
        return EMPTY_SET;
    }

    protected final void validateTrade(final ISign sign, final int index, final IEssentials ess) throws SignException {
        final String line = sign.getLine(index).trim();
        if (line.isEmpty()) {
            return;
        }
        final Trade trade = getTrade(sign, index, 0, ess);
        final Double money = trade.getMoney();
        if (money != null) {
            sign.setLine(index, Util.formatCurrency(money, ess));
        }
    }

    protected final void validateTrade(final ISign sign, final int amountIndex, final int itemIndex,
                                       final User player, final IEssentials ess) throws SignException {
        final Trade trade = getTrade(sign, amountIndex, itemIndex, player, ess);
        final ItemStack item = trade.getItemStack();
        sign.setLine(amountIndex, Integer.toString(item.getAmount()));
        sign.setLine(itemIndex, sign.getLine(itemIndex).trim());
    }

    protected final Trade getTrade(final ISign sign, final int amountIndex, final int itemIndex,
                                   final User player, final IEssentials ess) throws SignException {

        final ItemStack item = getItemStack(sign.getLine(itemIndex), 1, ess);
        final int amount = Math.min(getIntegerPositive(sign.getLine(amountIndex)), item.getType().getMaxStackSize() * player.getInventory().getSize());
        if (item.getTypeId() == 0 || amount < 1) {
            throw new SignException(Util.i18n("moreThanZero"));
        }
        item.setAmount(amount);
        return new Trade(item, ess);
    }

    protected final void validateInteger(final ISign sign, final int index) throws SignException {
        final String line = sign.getLine(index).trim();
        if (line.isEmpty()) {
            throw new SignException("Empty line " + index);
        }
        final int quantity = getIntegerPositive(line);
        sign.setLine(index, Integer.toString(quantity));
    }

    protected final int getIntegerPositive(final String line) throws SignException {
        final int quantity = getInteger(line);
        if (quantity < 1) {
            throw new SignException(Util.i18n("moreThanZero"));
        }
        return quantity;
    }

    protected final int getInteger(final String line) throws SignException {
        try {
            final int quantity = Integer.parseInt(line);

            return quantity;
        } catch (NumberFormatException ex) {
            throw new SignException("Invalid sign", ex);
        }
    }

    protected final ItemStack getItemStack(final String itemName, final int quantity, final IEssentials ess) throws SignException {
        try {
            final ItemStack item = ess.getItemDb().get(itemName);
            item.setAmount(quantity);
            return item;
        } catch (Exception ex) {
            throw new SignException(ex.getMessage(), ex);
        }
    }

    protected final Double getMoney(final String line) throws SignException {
        final boolean isMoney = line.matches("^[^0-9-\\.][\\.0-9]+$");
        return isMoney ? getDoublePositive(line.substring(1)) : null;
    }

    protected final Double getDoublePositive(final String line) throws SignException {
        final double quantity = getDouble(line);
        if (Math.round(quantity * 100.0) < 1.0) {
            throw new SignException(Util.i18n("moreThanZero"));
        }
        return quantity;
    }

    protected final Double getDouble(final String line) throws SignException {
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException ex) {
            throw new SignException(ex.getMessage(), ex);
        }
    }

    protected final Trade getTrade(final ISign sign, final int index, final IEssentials ess) throws SignException {
        return getTrade(sign, index, 1, ess);
    }

    protected final Trade getTrade(final ISign sign, final int index, final int decrement, final IEssentials ess) throws SignException {
        final String line = sign.getLine(index).trim();
        if (line.isEmpty()) {
            return new Trade(signName.toLowerCase() + "sign", ess);
        }

        final Double money = getMoney(line);
        if (money == null) {
            final String[] split = line.split("[ :]+", 2);
            if (split.length != 2) {
                throw new SignException(Util.i18n("invalidCharge"));
            }
            final int quantity = getIntegerPositive(split[0]);

            final String item = split[1].toLowerCase();
            if (item.equalsIgnoreCase("times")) {
                sign.setLine(index, (quantity - decrement) + " times");
                return new Trade(signName.toLowerCase() + "sign", ess);
            } else {
                final ItemStack stack = getItemStack(item, quantity, ess);
                sign.setLine(index, quantity + " " + item);
                return new Trade(stack, ess);
            }
        } else {
            return new Trade(money, ess);
        }
    }


    public interface ISign {
        String getLine(final int index);

        void setLine(final int index, final String text);

        Block getBlock();

        void updateSign();
    }

    static class EventSign implements ISign {
        private final transient SignChangeEvent event;
        private final transient Block block;

        public EventSign(final SignChangeEvent event) {
            this.event = event;
            this.block = event.getBlock();
        }

        public final String getLine(final int index) {
            return event.getLine(index);
        }

        public final void setLine(final int index, final String text) {
            event.setLine(index, text);
        }

        public Block getBlock() {
            return block;
        }

        public void updateSign() {
            return;
        }
    }

    static class BlockSign implements ISign {
        private final transient Sign sign;
        private final transient Block block;

        public BlockSign(final Block block) {
            this.block = block;
            this.sign = (Sign) block.getState();
        }

        public final String getLine(final int index) {
            return sign.getLine(index);
        }

        public final void setLine(final int index, final String text) {
            sign.setLine(index, text);
        }

        public final Block getBlock() {
            return block;
        }

        public final void updateSign() {
            sign.update();
        }
    }
}
