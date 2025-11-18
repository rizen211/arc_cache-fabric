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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(RecipeBook.class)
public class RecipeBookMixin {

    @Unique
    private final Set<RecipeEntry<?>> arc_cache$knownRecipes = new HashSet<>();

    @Unique
    private boolean arc_cache$isDirty = true;

    @Inject(method = "add", at = @At("HEAD"))
    private void onAddRecipe(RecipeEntry<?> recipe, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        if (arc_cache$knownRecipes.add(recipe)) {
            arc_cache$isDirty = true;
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemoveRecipe(RecipeEntry<?> recipe, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        if (arc_cache$knownRecipes.remove(recipe)) {
            arc_cache$isDirty = true;
        }
    }

    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    private void onCheckContains(RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        if (!arc_cache$isDirty && arc_cache$knownRecipes.contains(recipe)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "add", at = @At("TAIL"))
    private void afterAddRecipe(RecipeEntry<?> recipe, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled || !ARCCache.getConfig().enableRecipeBookCache) {
            return;
        }

        arc_cache$isDirty = false;
    }

    @Inject(method = "setOptions", at = @At("HEAD"))
    private void onSetOptions(CallbackInfo ci) {
        arc_cache$knownRecipes.clear();
        arc_cache$isDirty = true;
    }
}