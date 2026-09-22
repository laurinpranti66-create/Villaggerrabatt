package de.playground.villagerrabatt.listener;

import de.playground.villagerrabatt.VillagerRabattPlugin;
import de.playground.villagerrabatt.util.DiscountUtil;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

/**
 * Feuert genau in dem Moment, in dem ein Zombie-Villager fertig geheilt ist
 * und zum normalen Villager wird. Markiert ihn dauerhaft (persistiert über
 * Server-Neustarts) und rabattiert sofort etwaige bereits vorhandene Trades.
 */
public final class CureListener implements Listener {

    private final VillagerRabattPlugin plugin;

    public CureListener(VillagerRabattPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTransform(EntityTransformEvent event) {
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.CURED) {
            return;
        }
        if (!(event.getTransformedEntity() instanceof Villager villager)) {
            return;
        }

        DiscountUtil.markVillager(plugin, villager);
        DiscountUtil.applyDiscountToExisting(villager, plugin.getDiscountPercent(), plugin.isOnlyEmeraldTrades());
    }
}
