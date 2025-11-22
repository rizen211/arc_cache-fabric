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
import net.minecraft.recipe.book.RecipeBook;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.util.CacheStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(RecipeBook.class)
public class RecipeBookMixin {

    @Unique
    private final Map<RecipeEntry<?>, Boolean> arc_cache$recipeCache = new ConcurrentHashMap<>();

    @Inject(method = "add", at = @At("HEAD"))
    private void onAddRecipe(RecipeEntry<?> recipe, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        arc_cache$recipeCache.put(recipe, true);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemoveRecipe(RecipeEntry<?> recipe, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        arc_cache$recipeCache.put(recipe, false);
    }

    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    private void cacheContains(RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        Boolean cached = arc_cache$recipeCache.get(recipe);

        if (cached != null) {
            CacheStats.getInstance().recipeBookCacheHits.incrementAndGet();
            cir.setReturnValue(cached);
            return;
        }

        CacheStats.getInstance().recipeBookCacheMisses.incrementAndGet();
    }

    @Inject(method = "contains", at = @At("RETURN"))
    private void storeContainsResult(RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        Boolean result = cir.getReturnValue();
        arc_cache$recipeCache.put(recipe, result);

        CacheStats.getInstance().setRecipeBookCacheSize(arc_cache$recipeCache.size());
    }

    @Inject(method = "setOptions", at = @At("HEAD"))
    private void onSetOptions(CallbackInfo ci) {
        arc_cache$recipeCache.clear();
    }
}