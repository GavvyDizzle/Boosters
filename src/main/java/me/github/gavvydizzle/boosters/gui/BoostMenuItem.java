package me.github.gavvydizzle.boosters.gui;

import com.github.mittenmc.serverutils.gui.pages.ItemGenerator;
import lombok.Getter;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoostMenuItem implements Comparable<BoostMenuItem>, ItemGenerator {

    private final InventoryManager inventoryManager;
    @Getter private final Boost boost;
    private final ItemStack templateItem;

    public BoostMenuItem(InventoryManager inventoryManager, Boost boost, ItemStack templateItem) {
        this.inventoryManager = inventoryManager;
        this.boost = boost;
        this.templateItem = templateItem;
    }

    @Override
    public @NotNull ItemStack getMenuItem(Player player) {
        ItemStack itemStack = templateItem.clone();
        boost.replaceItemPlaceholders(itemStack, inventoryManager.getDateFormat());
        return itemStack;
    }

    @Override
    public @Nullable ItemStack getPlayerItem(Player player) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoostMenuItem boostMenuItem)) return false;
        return this.boost.equals(boostMenuItem.boost);
    }

    @Override
    public int compareTo(@NotNull BoostMenuItem o) {
        return this.boost.compareTo(o.boost);
    }
}
