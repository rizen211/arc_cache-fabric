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

package net.rizen.arc_cache.accessor;

public interface DirtyFlagAccessor {
    boolean arc_cache$isAdvancementsDirty();
    void arc_cache$setAdvancementsDirty(boolean dirty);
    boolean arc_cache$isRecipesDirty();
    void arc_cache$setRecipesDirty(boolean dirty);
    int arc_cache$getTicksSinceLastAdvancementUpdate();
    int arc_cache$getTicksSinceLastRecipeUpdate();
}