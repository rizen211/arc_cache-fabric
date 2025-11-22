/*
 * Copyright (C) 2025 rizen
 *
 * This file is part of A.R.C-Cache.
 *
 * A.R.C-Cache is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.rizen.arc_cache.util;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class InventorySnapshot {
    private int emptySlots;
    private int filledSlots;
    private int totalItems;

    public InventorySnapshot() {
        this.emptySlots = 0;
        this.filledSlots = 0;
        this.totalItems = 0;
    }

    public InventorySnapshot(PlayerInventory inventory) {
        update(inventory);
    }

    public void update(PlayerInventory inventory) {
        this.emptySlots = 0;
        this.filledSlots = 0;
        this.totalItems = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty()) {
                this.emptySlots++;
            } else {
                this.filledSlots++;
                this.totalItems += stack.getCount();
            }
        }
    }

    public boolean matches(PlayerInventory inventory) {
        int currentEmptySlots = 0;
        int currentFilledSlots = 0;
        int currentTotalItems = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty()) {
                currentEmptySlots++;
            } else {
                currentFilledSlots++;
                currentTotalItems += stack.getCount();
            }
        }

        return this.emptySlots == currentEmptySlots &&
                this.filledSlots == currentFilledSlots &&
                this.totalItems == currentTotalItems;
    }

    public int getEmptySlots() {
        return emptySlots;
    }

    public int getFilledSlots() {
        return filledSlots;
    }

    public int getTotalItems() {
        return totalItems;
    }
}