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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.accessor.DirtyFlagAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow
    public PlayerEntity player;

    @Inject(method = "setStack", at = @At("TAIL"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        arc_cache$markDirty();
    }

    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("TAIL"))
    private void onRemoveStack(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        arc_cache$markDirty();
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        arc_cache$markDirty();
    }

    @Unique
    private void arc_cache$markDirty() {
        if (!ARCCache.getConfig().enabled) return;

        if (player instanceof ServerPlayerEntity) {
            DirtyFlagAccessor accessor = (DirtyFlagAccessor) player;
            accessor.arc_cache$setAdvancementsDirty(true);
            accessor.arc_cache$setRecipesDirty(true);
        }
    }
}