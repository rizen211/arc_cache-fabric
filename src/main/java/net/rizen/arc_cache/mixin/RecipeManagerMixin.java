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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Unique
    private final Map<RecipeCacheKey, Optional<RecipeEntry<?>>> arc_cache$recipeCache =
            Collections.synchronizedMap(new LinkedHashMap<RecipeCacheKey, Optional<RecipeEntry<?>>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<RecipeCacheKey, Optional<RecipeEntry<?>>> eldest) {
                    return size() > ARCCache.getConfig().maxRecipeCacheSize;
                }
            });

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

        Optional<RecipeEntry<?>> cached = arc_cache$recipeCache.get(cacheKey);

        if (cached != null) {
            CacheStats.getInstance().recipeCacheHits.incrementAndGet();
            CacheStats.getInstance().recipeUpdatesSkipped.incrementAndGet();
            @SuppressWarnings("unchecked")
            Optional<RecipeEntry> result = (Optional) cached;
            cir.setReturnValue(result);
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
        @SuppressWarnings("unchecked")
        Optional<RecipeEntry<?>> cacheValue = (Optional) result;
        arc_cache$recipeCache.put(cacheKey, cacheValue);

        CacheStats.getInstance().setRecipeCacheSize(arc_cache$recipeCache.size());
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
}