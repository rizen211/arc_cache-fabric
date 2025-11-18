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

import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.util.CacheStats;
import net.rizen.arc_cache.util.RecipeCacheKey;
import net.rizen.arc_cache.util.RecipeInputHasher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Unique
    private final Map<RecipeCacheKey, Optional> arc_cache$recipeCache = new ConcurrentHashMap<>();

    @Unique
    private volatile boolean arc_cache$recipeCacheValid = true;

    @Inject(method = "getFirstMatch", at = @At("HEAD"), cancellable = true)
    private void cacheRecipeMatch(
            RecipeType recipeType,
            RecipeInput recipeInput,
            World world,
            CallbackInfoReturnable<Optional<RecipeEntry>> cir
    ) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeCache) {
            return;
        }

        if (!arc_cache$recipeCacheValid) {
            return;
        }

        int inputHash = RecipeInputHasher.hashRecipeInput(recipeInput);
        RecipeCacheKey cacheKey = new RecipeCacheKey(recipeType, inputHash);

        Optional cached = arc_cache$recipeCache.get(cacheKey);

        if (cached != null) {
            CacheStats.getInstance().recipeCacheHits.incrementAndGet();
            CacheStats.getInstance().recipeUpdatesSkipped.incrementAndGet();
            cir.setReturnValue(cached);
            return;
        }

        CacheStats.getInstance().recipeCacheMisses.incrementAndGet();
    }

    @Inject(method = "getFirstMatch", at = @At("RETURN"))
    private void storeRecipeMatchResult(
            RecipeType recipeType,
            RecipeInput recipeInput,
            World world,
            CallbackInfoReturnable<Optional<RecipeEntry>> cir
    ) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeCache) {
            return;
        }

        if (!arc_cache$recipeCacheValid) {
            return;
        }

        int inputHash = RecipeInputHasher.hashRecipeInput(recipeInput);
        RecipeCacheKey cacheKey = new RecipeCacheKey(recipeType, inputHash);

        Optional<RecipeEntry> result = cir.getReturnValue();
        arc_cache$recipeCache.put(cacheKey, result);

        int maxSize = ARCCache.getConfig().maxRecipeCacheSize;
        if (arc_cache$recipeCache.size() > maxSize) {
            arc_cache$trimCache(maxSize);
        }

        CacheStats.getInstance().recipeCacheSize = arc_cache$recipeCache.size();
    }

    @Inject(method = "apply*", at = @At("HEAD"))
    private void onRecipeReload(CallbackInfo ci) {
        arc_cache$recipeCache.clear();
        arc_cache$recipeCacheValid = false;
        ARCCache.LOGGER.info("Recipe cache invalidated");
    }

    @Inject(method = "apply*", at = @At("TAIL"))
    private void onRecipeReloadComplete(CallbackInfo ci) {
        arc_cache$recipeCacheValid = true;
        ARCCache.LOGGER.info("Recipe cache revalidated");
    }

    @Unique
    private void arc_cache$trimCache(int targetSize) {
        if (arc_cache$recipeCache.size() <= targetSize) {
            return;
        }

        int toRemove = arc_cache$recipeCache.size() - targetSize;
        arc_cache$recipeCache.keySet().stream()
                .limit(toRemove)
                .forEach(arc_cache$recipeCache::remove);
    }
}