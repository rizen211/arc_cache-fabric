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

import net.minecraft.server.network.ServerPlayerEntity;
import net.rizen.arc_cache.ARCCache;
import net.rizen.arc_cache.accessor.DirtyFlagAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements DirtyFlagAccessor {

	@Unique
	private boolean arc_cache$advancementsDirty = true;

	@Unique
	private boolean arc_cache$recipesDirty = true;

	@Unique
	private int arc_cache$ticksSinceLastAdvancementUpdate = 0;

	@Unique
	private int arc_cache$ticksSinceLastRecipeUpdate = 0;

	@Inject(method = "onSpawn", at = @At("TAIL"))
	private void onPlayerJoin(CallbackInfo ci) {
		if (!ARCCache.getConfig().enabled) return;

		arc_cache$advancementsDirty = true;
		arc_cache$recipesDirty = true;
		arc_cache$ticksSinceLastAdvancementUpdate = 0;
		arc_cache$ticksSinceLastRecipeUpdate = 0;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (!ARCCache.getConfig().enabled) return;

		if (arc_cache$advancementsDirty) {
			arc_cache$ticksSinceLastAdvancementUpdate++;
		}
		if (arc_cache$recipesDirty) {
			arc_cache$ticksSinceLastRecipeUpdate++;
		}
	}

	@Override
	public boolean arc_cache$isAdvancementsDirty() {
		return arc_cache$advancementsDirty;
	}

	@Override
	public void arc_cache$setAdvancementsDirty(boolean dirty) {
		arc_cache$advancementsDirty = dirty;
		if (dirty) {
			arc_cache$ticksSinceLastAdvancementUpdate = 0;
		}
	}

	@Override
	public boolean arc_cache$isRecipesDirty() {
		return arc_cache$recipesDirty;
	}

	@Override
	public void arc_cache$setRecipesDirty(boolean dirty) {
		arc_cache$recipesDirty = dirty;
		if (dirty) {
			arc_cache$ticksSinceLastRecipeUpdate = 0;
		}
	}

	@Override
	public int arc_cache$getTicksSinceLastAdvancementUpdate() {
		return arc_cache$ticksSinceLastAdvancementUpdate;
	}

	@Override
	public int arc_cache$getTicksSinceLastRecipeUpdate() {
		return arc_cache$ticksSinceLastRecipeUpdate;
	}
}