package de.playground.villagerrabatt.task;

import de.playground.villagerrabatt.VillagerRabattPlugin;
import de.playground.villagerrabatt.util.DiscountUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Läuft periodisch und sorgt dafür, dass das "Rabatt-Händler"-Namensschild
 * NICHT permanent über jedem markierten Villager schwebt (das würde bei
 * vielen geheilten Villagern die ganze Welt mit Namensschildern zupflastern),
 * sondern nur dann eingeblendet wird, während irgendein Spieler den Villager
 * gerade direkt anvisiert (per Raytrace aus der Blickrichtung).
 */
public final class NametagVisibilityTask extends BukkitRunnable {

    private final VillagerRabattPlugin plugin;
    private final Set<UUID> currentlyVisible = new HashSet<>();

    public NametagVisibilityTask(VillagerRabattPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.isShowNametag()) {
            return;
        }

        double maxDistance = plugin.getNametagMaxDistance();
        Set<UUID> newlyVisible = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location eye = player.getEyeLocation();
            Vector direction = eye.getDirection();
            RayTraceResult result = player.getWorld().rayTraceEntities(eye, direction, maxDistance, 0.3,
                    entity -> entity instanceof Villager villager && DiscountUtil.isMarked(plugin, villager));
            if (result != null && result.getHitEntity() != null) {
                newlyVisible.add(result.getHitEntity().getUniqueId());
            }
        }

        // Nametag bei Villagern ausblenden, die gerade aus dem Blick gewandert sind.
        for (UUID uuid : currentlyVisible) {
            if (!newlyVisible.contains(uuid)) {
                setVisible(uuid, false);
            }
        }
        // Nametag bei neu anvisierten Villagern einblenden.
        for (UUID uuid : newlyVisible) {
            if (!currentlyVisible.contains(uuid)) {
                setVisible(uuid, true);
            }
        }

        currentlyVisible.clear();
        currentlyVisible.addAll(newlyVisible);
    }

    private void setVisible(UUID uuid, boolean visible) {
        if (Bukkit.getEntity(uuid) instanceof Villager villager) {
            villager.setCustomNameVisible(visible);
        }
    }
}
