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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.accessor.DirtyFlagAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatHandler.class)
public class ServerStatHandlerMixin {

    @Inject(method = "setStat", at = @At("TAIL"))
    private void onSetStat(PlayerEntity player, Stat<?> stat, int value, CallbackInfo ci) {
        if (!ARCCache.getConfig().enabled) return;

        if (player instanceof ServerPlayerEntity) {
            DirtyFlagAccessor accessor = (DirtyFlagAccessor) player;
            accessor.arc_cache$setAdvancementsDirty(true);
        }
    }
}