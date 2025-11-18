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
                ARCCache.LOGGER.info("Loaded configuration from {}", CONFIG_PATH);
                return GSON.fromJson(json, ARCCacheConfig.class);
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
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            ARCCache.LOGGER.info("Saved configuration to {}", CONFIG_PATH);
        } catch (IOException e) {
            ARCCache.LOGGER.error("Failed to save config", e);
        }
    }
}