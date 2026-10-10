package io.github.addoncommunity.galactifun.base.items;

import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuit;
import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuitUpgrade;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class SpaceSuitUpgrader extends AContainer {

    public SpaceSuitUpgrader(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Nullable
    @Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        ItemStack suitStack = null;
        SpaceSuit suit = null;
        int suitSlot = -1;

        ItemStack upgradeStack = null;
        SpaceSuitUpgrade upgrade = null;
        int upgradeSlot = -1;

        for (int slot : this.getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);
            if (item == null || !item.hasItemMeta()) continue;

            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if (suit == null && sfItem instanceof SpaceSuit s) {
                suit = s;
                suitStack = item;
                suitSlot = slot;
            } else if (upgrade == null && sfItem instanceof SpaceSuitUpgrade u) {
                upgrade = u;
                upgradeStack = item;
                upgradeSlot = slot;
            }

            if (suit == null || upgrade == null) continue;

            ItemMeta meta = suitStack.getItemMeta();
            if (!upgrade.addTo(meta, suit.maxUpgrades())) continue;

            ItemStack newSuit = suitStack.clone();
            newSuit.setItemMeta(meta);

            // Copy of the consumed upgrade (amount 1) for the recipe, so we never hand out a live/0-amount stack
            ItemStack consumedUpgrade = upgradeStack.clone();
            consumedUpgrade.setAmount(1);

            // FIX: never call setAmount(0) on a live slot item. Let Slimefun remove it properly,
            // otherwise Paper 1.21+ fails to serialize the 0-amount stack on autosave.
            inv.consumeItem(upgradeSlot, 1);
            inv.replaceExistingItem(suitSlot, null);

            return new MachineRecipe(5 / this.getSpeed(), new ItemStack[]{consumedUpgrade}, new ItemStack[]{newSuit});
        }
        return null;
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.ANVIL);
    }

    @Nonnull
    @Override
    public String getMachineIdentifier() {
        return "SPACE_SUIT_UPGRADER";
    }
}