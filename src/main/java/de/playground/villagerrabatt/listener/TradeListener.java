package de.playground.villagerrabatt.listener;

import de.playground.villagerrabatt.VillagerRabattPlugin;
import de.playground.villagerrabatt.util.DiscountUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

/**
 * Zwei Aufgaben:
 * 1) Jeder NEU generierte Trade (Level-Up, Restock) eines markierten
 *    Villagers wird sofort rabattiert - so bleibt der Rabatt dauerhaft
 *    bestehen, auch wenn der Villager mit der Zeit neue Angebote bekommt.
 * 2) Sichtbarer Hinweis im Chat, sobald irgendein Spieler das Handelsfenster
 *    eines markierten Villagers öffnet.
 */
public final class TradeListener implements Listener {

    private final VillagerRabattPlugin plugin;

    public TradeListener(VillagerRabattPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        if (!DiscountUtil.isMarked(plugin, villager)) {
            return;
        }
        MerchantRecipe recipe = event.getRecipe();
        DiscountUtil.discountRecipeInPlace(recipe, plugin.getDiscountPercent(), plugin.isOnlyEmeraldTrades());
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(merchantInventory.getMerchant() instanceof Villager villager)) {
            return;
        }
        if (!DiscountUtil.isMarked(plugin, villager)) {
            return;
        }

        String message = plugin.getTradeOpenMessage().replace("{percent}", String.valueOf(plugin.getDiscountPercent()));
        player.sendMessage(message);
    }
}
