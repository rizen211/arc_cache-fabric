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

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class RecipeInputHasher {

    public static int hashRecipeInput(RecipeInput input) {
        if (input == null) {
            return 0;
        }

        int hash = 0;
        int itemCount = 0;

        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            int stackHash = hashItemStack(stack);
            hash ^= stackHash;
            hash ^= Integer.rotateLeft(stackHash, i % 32);
            itemCount++;
        }

        hash = hash * 31 + itemCount;
        return hash;
    }

    private static int hashItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int hash = 1;
        hash = 31 * hash + stack.getItem().hashCode();
        hash = 31 * hash + stack.getCount();
        hash = 31 * hash + stack.getComponents().hashCode();

        return hash;
    }

    public static int hashRecipeInputOrdered(RecipeInput input) {
        if (input == null) {
            return 0;
        }

        int hash = 1;

        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            hash = 31 * hash + hashItemStack(stack);

            if (!stack.isEmpty()) {
                hash = 31 * hash + i;
            }
        }

        return hash;
    }

    public static int hashSingleItem(ItemStack stack) {
        return hashItemStack(stack);
    }
}