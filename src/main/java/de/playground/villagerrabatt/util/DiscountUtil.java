package de.playground.villagerrabatt.util;

import de.playground.villagerrabatt.VillagerRabattPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Kernlogik: Markieren eines Villagers (persistent, überlebt Neustarts) und
 * Anwenden des Rabatts auf seine Emerald-Kaufpreise - sowohl auf bereits
 * vorhandene Trades als auch auf einzelne, neu generierte Trades.
 */
public final class DiscountUtil {

    private DiscountUtil() {
    }

    public static boolean isMarked(VillagerRabattPlugin plugin, Villager villager) {
        Byte value = villager.getPersistentDataContainer().get(plugin.getMarkedKey(), PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public static void markVillager(VillagerRabattPlugin plugin, Villager villager) {
        villager.getPersistentDataContainer().set(plugin.getMarkedKey(), PersistentDataType.BYTE, (byte) 1);
        if (plugin.isShowNametag()) {
            villager.setCustomName(plugin.getNametag());
            // Standardmäßig unsichtbar - wird von NametagVisibilityTask nur
            // eingeblendet, während ein Spieler den Villager gerade anvisiert.
            villager.setCustomNameVisible(false);
        }
    }

    /**
     * Rabattiert alle bereits vorhandenen Trades eines Villagers - z.B. direkt
     * nach dem Entzombifizieren (falls schon Trades da sind) oder beim
     * rückwirkenden Markieren per Befehl.
     */
    public static boolean applyDiscountToExisting(Villager villager, int percent, boolean onlyEmeraldTrades) {
        List<MerchantRecipe> recipes = villager.getRecipes();
        if (recipes.isEmpty()) {
            return false;
        }
        boolean anyChanged = false;
        for (MerchantRecipe recipe : recipes) {
            if (discountRecipeInPlace(recipe, percent, onlyEmeraldTrades)) {
                anyChanged = true;
            }
        }
        if (anyChanged) {
            villager.setRecipes(recipes);
        }
        return anyChanged;
    }

    /**
     * Rabattiert ein einzelnes Rezept in-place (per setIngredients). Wird
     * sowohl von applyDiscountToExisting als auch direkt vom
     * VillagerAcquireTradeEvent (neu generierte Trades) verwendet, damit der
     * Rabatt auch nach Level-Ups/Restocks dauerhaft erhalten bleibt.
     */
    public static boolean discountRecipeInPlace(MerchantRecipe recipe, int percent, boolean onlyEmeraldTrades) {
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            return false;
        }

        ItemStack primary = ingredients.get(0);
        boolean primaryIsEmerald = primary != null && isEmeraldType(primary.getType());
        if (onlyEmeraldTrades && !primaryIsEmerald) {
            return false;
        }

        List<ItemStack> newIngredients = new ArrayList<>(ingredients.size());
        boolean changed = false;
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && isEmeraldType(ingredient.getType())) {
                int amount = ingredient.getAmount();
                int discounted = Math.max(1, (int) Math.round(amount * (100 - percent) / 100.0));
                if (discounted != amount) {
                    ItemStack copy = ingredient.clone();
                    copy.setAmount(discounted);
                    newIngredients.add(copy);
                    changed = true;
                    continue;
                }
            }
            newIngredients.add(ingredient);
        }

        if (changed) {
            recipe.setIngredients(newIngredients);
        }
        return changed;
    }

    private static boolean isEmeraldType(Material material) {
        return material == Material.EMERALD || material == Material.EMERALD_BLOCK;
    }
}
