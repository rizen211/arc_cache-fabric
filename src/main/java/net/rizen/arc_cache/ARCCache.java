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

package net.rizen.arc_cache;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.rizen.arc_cache.config.ARCCacheConfig;
import net.rizen.arc_cache.util.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ARCCache implements ModInitializer {
	public static final String MOD_ID = "arc_cache";
	public static final String MOD_NAME = "A.R.C-Cache";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private static ARCCacheConfig config;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MOD_NAME);

		config = ARCCacheConfig.load();
		registerCommands();

		if (config.enabled) {
			LOGGER.info("A.R.C-Cache optimizations enabled:");
			LOGGER.info("  - Advancement Cache: {}", config.enableAdvancementCache);
			LOGGER.info("  - Recipe Cache: {}", config.enableRecipeCache);
			LOGGER.info("  - RecipeBook Cache: {}", config.enableRecipeBookCache);
		} else {
			LOGGER.warn("A.R.C-Cache is DISABLED via config");
		}
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("arc-cache")
					.requires(source -> source.hasPermissionLevel(2))
					.then(CommandManager.literal("stats")
							.executes(this::executeStatsCommand)
					)
					.then(CommandManager.literal("reset")
							.executes(context -> {
								CacheStats.getInstance().reset();
								context.getSource().sendFeedback(() ->
										Text.literal("§aA.R.C-Cache statistics reset"), true);
								return Command.SINGLE_SUCCESS;
							})
					)
					.then(CommandManager.literal("toggle")
							.then(createToggleCommand("enabled",
									() -> config.enabled,
									val -> config.enabled = val,
									"A.R.C-Cache"))
							.then(createToggleCommand("advancement-cache",
									() -> config.enableAdvancementCache,
									val -> config.enableAdvancementCache = val,
									"Advancement cache"))
							.then(createToggleCommand("recipe-cache",
									() -> config.enableRecipeCache,
									val -> config.enableRecipeCache = val,
									"Recipe cache"))
							.then(createToggleCommand("recipe-book-cache",
									() -> config.enableRecipeBookCache,
									val -> config.enableRecipeBookCache = val,
									"RecipeBook cache"))
					)
			);
		});
	}

	private int executeStatsCommand(CommandContext<ServerCommandSource> context) {
		var source = context.getSource();
		var stats = CacheStats.getInstance();

		source.sendFeedback(() -> Text.literal("§6=== A.R.C-Cache Statistics ==="), false);
		source.sendFeedback(() -> Text.literal(String.format(
				"§eAdvancement Cache: §f%,d hits / %,d misses (§a%.1f%% hit rate§f)",
				stats.advancementCacheHits.get(),
				stats.advancementCacheMisses.get(),
				stats.getAdvancementHitRate()
		)), false);
		source.sendFeedback(() -> Text.literal(String.format(
				"§eRecipe Cache: §f%,d hits / %,d misses (§a%.1f%% hit rate§f)",
				stats.recipeCacheHits.get(),
				stats.recipeCacheMisses.get(),
				stats.getRecipeHitRate()
		)), false);
		source.sendFeedback(() -> Text.literal(String.format(
				"§eRecipeBook Cache: §f%,d hits / %,d misses (§a%.1f%% hit rate§f)",
				stats.recipeBookCacheHits.get(),
				stats.recipeBookCacheMisses.get(),
				stats.getRecipeBookHitRate()
		)), false);
		source.sendFeedback(() -> Text.literal(String.format(
				"§eSkipped Updates: §f%,d advancement / %,d recipe",
				stats.advancementUpdatesSkipped.get(),
				stats.recipeUpdatesSkipped.get()
		)), false);
		source.sendFeedback(() -> Text.literal(String.format(
				"§eCache Size: §f%,d advancements / %,d recipes / %,d recipe book",
				stats.getAdvancementCacheSize(),
				stats.getRecipeCacheSize(),
				stats.getRecipeBookCacheSize()
		)), false);

		return Command.SINGLE_SUCCESS;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> createToggleCommand(
			String name,
			Supplier<Boolean> getter,
			Consumer<Boolean> setter,
			String displayName
	) {
		return CommandManager.literal(name)
				.executes(context -> {
					boolean newValue = !getter.get();
					setter.accept(newValue);
					config.save();
					context.getSource().sendFeedback(() ->
							Text.literal(String.format("§e%s %s",
									displayName, newValue ? "§aenabled" : "§cdisabled")), true);
					return Command.SINGLE_SUCCESS;
				});
	}

	public static ARCCacheConfig getConfig() {
		return config;
	}
}