package de.playground.villagerrabatt.command;

import de.playground.villagerrabatt.VillagerRabattPlugin;
import de.playground.villagerrabatt.util.DiscountUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * /vrabatt markall [radius|world] - markiert rückwirkend bereits existierende
 *   (schon vor dem Plugin geheilte) Villager im Radius um den Spieler, oder
 *   alle geladenen Villager der Welt.
 * /vrabatt reload - lädt die config.yml neu.
 */
public final class VillagerRabattCommand implements CommandExecutor, TabCompleter {

    private final VillagerRabattPlugin plugin;

    public VillagerRabattCommand(VillagerRabattPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(VillagerRabattPlugin.color("&cNutzung: &7/vrabatt markall [radius|world] &8| &7/vrabatt reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigValues();
            sender.sendMessage(VillagerRabattPlugin.color("&aKonfiguration neu geladen."));
            return true;
        }

        if (args[0].equalsIgnoreCase("markall")) {
            handleMarkAll(sender, args);
            return true;
        }

        sender.sendMessage(VillagerRabattPlugin.color("&cUnbekannter Unterbefehl. Nutzung: &7/vrabatt markall [radius|world] &8| &7/vrabatt reload"));
        return true;
    }

    private void handleMarkAll(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(VillagerRabattPlugin.color("&cDieser Befehl braucht einen Spieler-Standort und kann nicht von der Konsole genutzt werden."));
            return;
        }

        Collection<Villager> villagers;

        if (args.length >= 2 && args[1].equalsIgnoreCase("world")) {
            villagers = player.getWorld().getEntitiesByClass(Villager.class);
        } else {
            int radius = plugin.getMarkRadiusDefault();
            if (args.length >= 2) {
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(VillagerRabattPlugin.color("&cUngültiger Radius: &f" + args[1]));
                    return;
                }
            }
            List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
            villagers = nearby.stream()
                    .filter(e -> e instanceof Villager)
                    .map(e -> (Villager) e)
                    .toList();
        }

        int newlyMarked = 0;
        int priceAdjusted = 0;
        for (Villager villager : villagers) {
            boolean wasMarked = DiscountUtil.isMarked(plugin, villager);
            DiscountUtil.markVillager(plugin, villager);
            if (!wasMarked) {
                newlyMarked++;
            }
            if (DiscountUtil.applyDiscountToExisting(villager, plugin.getDiscountPercent(), plugin.isOnlyEmeraldTrades())) {
                priceAdjusted++;
            }
        }

        sender.sendMessage(VillagerRabattPlugin.color("&a" + villagers.size() + " Villager geprüft (&f" + newlyMarked
                + " &aneu markiert, &f" + priceAdjusted + " &amit direkt angepassten Preisen)."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("markall", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("markall")) {
            return Arrays.asList("world", String.valueOf(plugin.getMarkRadiusDefault()));
        }
        return Collections.emptyList();
    }
}
