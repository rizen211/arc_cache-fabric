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

import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {
    private static final CacheStats INSTANCE = new CacheStats();

    public final AtomicLong advancementCacheHits = new AtomicLong(0);
    public final AtomicLong advancementCacheMisses = new AtomicLong(0);
    public final AtomicLong advancementUpdatesSkipped = new AtomicLong(0);
    public volatile int advancementCacheSize = 0;

    public final AtomicLong recipeCacheHits = new AtomicLong(0);
    public final AtomicLong recipeCacheMisses = new AtomicLong(0);
    public final AtomicLong recipeUpdatesSkipped = new AtomicLong(0);
    public volatile int recipeCacheSize = 0;

    public final AtomicLong recipeBookCacheHits = new AtomicLong(0);
    public final AtomicLong recipeBookCacheMisses = new AtomicLong(0);
    public volatile int recipeBookCacheSize = 0;

    public final AtomicLong totalTicksSaved = new AtomicLong(0);

    private CacheStats() {}

    public static CacheStats getInstance() {
        return INSTANCE;
    }

    public double getAdvancementHitRate() {
        long total = advancementCacheHits.get() + advancementCacheMisses.get();
        if (total == 0) return 0.0;
        return (advancementCacheHits.get() * 100.0) / total;
    }

    public double getRecipeHitRate() {
        long total = recipeCacheHits.get() + recipeCacheMisses.get();
        if (total == 0) return 0.0;
        return (recipeCacheHits.get() * 100.0) / total;
    }

    public double getRecipeBookHitRate() {
        long total = recipeBookCacheHits.get() + recipeBookCacheMisses.get();
        if (total == 0) return 0.0;
        return (recipeBookCacheHits.get() * 100.0) / total;
    }

    public void reset() {
        advancementCacheHits.set(0);
        advancementCacheMisses.set(0);
        advancementUpdatesSkipped.set(0);
        recipeCacheHits.set(0);
        recipeCacheMisses.set(0);
        recipeUpdatesSkipped.set(0);
        recipeBookCacheHits.set(0);
        recipeBookCacheMisses.set(0);
        totalTicksSaved.set(0);
        advancementCacheSize = 0;
        recipeCacheSize = 0;
        recipeBookCacheSize = 0;
    }

    public String getFormattedStats() {
        return String.format(
                "A.R.C-Cache: Adv %.1f%% (%d/%d) | Recipe %.1f%% (%d/%d) | Book %.1f%% (%d/%d) | Skipped: %d/%d",
                getAdvancementHitRate(),
                advancementCacheHits.get(),
                advancementCacheHits.get() + advancementCacheMisses.get(),
                getRecipeHitRate(),
                recipeCacheHits.get(),
                recipeCacheHits.get() + recipeCacheMisses.get(),
                getRecipeBookHitRate(),
                recipeBookCacheHits.get(),
                recipeBookCacheHits.get() + recipeBookCacheMisses.get(),
                advancementUpdatesSkipped.get(),
                recipeUpdatesSkipped.get()
        );
    }
}