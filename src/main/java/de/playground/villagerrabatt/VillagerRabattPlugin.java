package de.playground.villagerrabatt;

import de.playground.villagerrabatt.command.VillagerRabattCommand;
import de.playground.villagerrabatt.listener.CureListener;
import de.playground.villagerrabatt.listener.TradeListener;
import de.playground.villagerrabatt.task.NametagVisibilityTask;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VillagerRabattPlugin extends JavaPlugin {

    private NamespacedKey markedKey;

    private int discountPercent;
    private boolean onlyEmeraldTrades;
    private boolean showNametag;
    private String nametag;
    private String tradeOpenMessage;
    private int markRadiusDefault;
    private double nametagMaxDistance;
    private long nametagCheckIntervalTicks;

    private BukkitTask nametagTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        markedKey = new NamespacedKey(this, "rabatt_villager");
        reloadConfigValues();

        getServer().getPluginManager().registerEvents(new CureListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);

        VillagerRabattCommand executor = new VillagerRabattCommand(this);
        getCommand("villagerrabatt").setExecutor(executor);
        getCommand("villagerrabatt").setTabCompleter(executor);

        nametagTask = new NametagVisibilityTask(this).runTaskTimer(this, 0L, nametagCheckIntervalTicks);

        getLogger().info("VillagerRabatt aktiviert (Rabatt: " + discountPercent + "%).");
    }

    @Override
    public void onDisable() {
        if (nametagTask != null) {
            nametagTask.cancel();
        }
    }

    public void reloadConfigValues() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        discountPercent = cfg.getInt("discount-percent", 50);
        onlyEmeraldTrades = cfg.getBoolean("only-emerald-trades", true);
        showNametag = cfg.getBoolean("show-nametag", true);
        nametag = color(cfg.getString("nametag", "&a&lRabatt-Händler"));
        tradeOpenMessage = color(cfg.getString("trade-open-message",
                "&7Dieser Händler hat einen dauerhaften Rabatt von &a{percent}%&7 auf alle Kauf-Preise (gilt für jeden Spieler)."));
        markRadiusDefault = cfg.getInt("mark-radius-default", 50);
        nametagMaxDistance = cfg.getDouble("nametag-max-distance", 6.0);
        nametagCheckIntervalTicks = Math.max(1L, cfg.getLong("nametag-check-interval-ticks", 10L));
    }

    public static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public NamespacedKey getMarkedKey() {
        return markedKey;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public boolean isOnlyEmeraldTrades() {
        return onlyEmeraldTrades;
    }

    public boolean isShowNametag() {
        return showNametag;
    }

    public String getNametag() {
        return nametag;
    }

    public String getTradeOpenMessage() {
        return tradeOpenMessage;
    }

    public int getMarkRadiusDefault() {
        return markRadiusDefault;
    }

    public double getNametagMaxDistance() {
        return nametagMaxDistance;
    }
}
