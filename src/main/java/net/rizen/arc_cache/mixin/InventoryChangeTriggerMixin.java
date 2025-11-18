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

package net.rizen.arc_cache.mixin;

import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.util.CacheStats;
import net.rizen.arc_cache.util.InventorySnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(InventoryChangedCriterion.class)
public class InventoryChangeTriggerMixin {

    @Unique
    private final Map<ServerPlayerEntity, InventorySnapshot> arc_cache$playerSnapshots = new HashMap<>();

    @Inject(method = "trigger", at = @At("HEAD"), cancellable = true)
    private void optimizeInventoryTrigger(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableAdvancementCache) {
            return;
        }

        InventorySnapshot snapshot = arc_cache$playerSnapshots.get(player);

        if (snapshot == null) {
            snapshot = new InventorySnapshot();
            arc_cache$playerSnapshots.put(player, snapshot);
            snapshot.update(inventory);
            CacheStats.getInstance().advancementCacheMisses.incrementAndGet();
            return;
        }

        if (arc_cache$shouldSkipTrigger(inventory, snapshot)) {
            CacheStats.getInstance().advancementCacheHits.incrementAndGet();
            CacheStats.getInstance().advancementUpdatesSkipped.incrementAndGet();
            ci.cancel();
            return;
        }

        snapshot.update(inventory);
        CacheStats.getInstance().advancementCacheMisses.incrementAndGet();
        CacheStats.getInstance().advancementCacheSize = arc_cache$playerSnapshots.size();
    }

    @Unique
    private boolean arc_cache$shouldSkipTrigger(PlayerInventory inventory, InventorySnapshot snapshot) {
        int emptySlots = 0;
        int filledSlots = 0;
        int totalItems = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);

            if (current.isEmpty()) {
                emptySlots++;
            } else {
                filledSlots++;
                totalItems += current.getCount();
            }
        }

        return snapshot.emptySlots == emptySlots &&
                snapshot.filledSlots == filledSlots &&
                snapshot.totalItems == totalItems;
    }
}