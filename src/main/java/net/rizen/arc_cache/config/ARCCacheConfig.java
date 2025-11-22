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

package net.rizen.arc_cache.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.rizen.arc_cache.ARCCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ARCCacheConfig {
    private static final Path CONFIG_PATH = Paths.get("config", "arc_cache.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int MIN_CACHE_SIZE = 100;
    private static final int MAX_CACHE_SIZE = 100000;
    private static final int MIN_DELAY = 1;
    private static final int MAX_DELAY = 100;

    public boolean enabled = true;
    public boolean enableAdvancementCache = true;
    public boolean enableRecipeCache = true;
    public boolean enableRecipeBookCache = true;
    public boolean showDebugStats = false;
    public int cacheInvalidationDelay = 1;
    public int maxAdvancementCacheSize = 10000;
    public int maxRecipeCacheSize = 5000;

    public static ARCCacheConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                ARCCacheConfig config = GSON.fromJson(json, ARCCacheConfig.class);
                config.validate();
                ARCCache.LOGGER.info("Loaded configuration from {}", CONFIG_PATH);
                return config;
            }
        } catch (IOException e) {
            ARCCache.LOGGER.error("Failed to load config, using defaults", e);
        }

        ARCCacheConfig config = new ARCCacheConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            validate();
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            ARCCache.LOGGER.info("Saved configuration to {}", CONFIG_PATH);
        } catch (IOException e) {
            ARCCache.LOGGER.error("Failed to save config", e);
        }
    }

    private void validate() {
        boolean changed = false;

        if (maxAdvancementCacheSize < MIN_CACHE_SIZE || maxAdvancementCacheSize > MAX_CACHE_SIZE) {
            ARCCache.LOGGER.warn("Invalid maxAdvancementCacheSize: {}. Clamping to range [{}, {}]",
                    maxAdvancementCacheSize, MIN_CACHE_SIZE, MAX_CACHE_SIZE);
            maxAdvancementCacheSize = Math.max(MIN_CACHE_SIZE, Math.min(MAX_CACHE_SIZE, maxAdvancementCacheSize));
            changed = true;
        }

        if (maxRecipeCacheSize < MIN_CACHE_SIZE || maxRecipeCacheSize > MAX_CACHE_SIZE) {
            ARCCache.LOGGER.warn("Invalid maxRecipeCacheSize: {}. Clamping to range [{}, {}]",
                    maxRecipeCacheSize, MIN_CACHE_SIZE, MAX_CACHE_SIZE);
            maxRecipeCacheSize = Math.max(MIN_CACHE_SIZE, Math.min(MAX_CACHE_SIZE, maxRecipeCacheSize));
            changed = true;
        }

        if (cacheInvalidationDelay < MIN_DELAY || cacheInvalidationDelay > MAX_DELAY) {
            ARCCache.LOGGER.warn("Invalid cacheInvalidationDelay: {}. Clamping to range [{}, {}]",
                    cacheInvalidationDelay, MIN_DELAY, MAX_DELAY);
            cacheInvalidationDelay = Math.max(MIN_DELAY, Math.min(MAX_DELAY, cacheInvalidationDelay));
            changed = true;
        }

        if (changed) {
            ARCCache.LOGGER.info("Config values were adjusted to valid ranges");
        }
    }
}