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

import net.minecraft.recipe.RecipeType;

public class RecipeCacheKey {
    private final RecipeType<?> type;
    private final int inputHash;
    private final int hashCode;

    public RecipeCacheKey(RecipeType<?> type, int inputHash) {
        this.type = type;
        this.inputHash = inputHash;

        int hash = 1;
        hash = 31 * hash + type.hashCode();
        hash = 31 * hash + inputHash;
        this.hashCode = hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecipeCacheKey)) return false;
        RecipeCacheKey other = (RecipeCacheKey) obj;
        return this.inputHash == other.inputHash && this.type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}