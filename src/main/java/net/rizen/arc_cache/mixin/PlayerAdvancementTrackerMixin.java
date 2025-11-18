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

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.accessor.DirtyFlagAccessor;
import net.rizen.arc_cache.util.CacheStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    private ServerPlayerEntity owner;

    @Unique
    private final Map<AdvancementEntry, Boolean> arc_cache$completionCache = new IdentityHashMap<>();

    @Unique
    private boolean arc_cache$cacheValid = true;

    @Inject(method = "grantCriterion", at = @At("HEAD"))
    private void onGrantCriterion(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!ARCCache.getConfig().enabled) return;

        arc_cache$completionCache.remove(advancement);
        arc_cache$cacheValid = false;
        ((DirtyFlagAccessor) owner).arc_cache$setAdvancementsDirty(true);
    }

    @Inject(method = "revokeCriterion", at = @At("HEAD"))
    private void onRevokeCriterion(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!ARCCache.getConfig().enabled) return;

        arc_cache$completionCache.remove(advancement);
        arc_cache$cacheValid = false;
        ((DirtyFlagAccessor) owner).arc_cache$setAdvancementsDirty(true);
    }

    @Inject(method = "getProgress", at = @At("RETURN"))
    private void onGetProgress(AdvancementEntry advancement, CallbackInfoReturnable<AdvancementProgress> cir) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableAdvancementCache) {
            return;
        }

        AdvancementProgress progress = cir.getReturnValue();
        if (progress != null) {
            Boolean cachedValue = arc_cache$completionCache.get(advancement);
            boolean currentValue = progress.isDone();

            if (cachedValue != null && cachedValue == currentValue) {
                CacheStats.getInstance().advancementCacheHits.incrementAndGet();
                CacheStats.getInstance().advancementUpdatesSkipped.incrementAndGet();
            } else {
                arc_cache$completionCache.put(advancement, currentValue);
                CacheStats.getInstance().advancementCacheMisses.incrementAndGet();
            }

            CacheStats.getInstance().advancementCacheSize = arc_cache$completionCache.size();
        }
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void onReload(CallbackInfo ci) {
        arc_cache$completionCache.clear();
        arc_cache$cacheValid = false;
        ((DirtyFlagAccessor) owner).arc_cache$setAdvancementsDirty(true);
    }

    @Inject(method = "reload", at = @At("TAIL"))
    private void afterReload(CallbackInfo ci) {
        arc_cache$cacheValid = true;
    }
}