package me.github.gavvydizzle.boosters.boost;

import com.github.mittenmc.serverutils.RepeatingTask;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.event.CommandDropsListener;
import me.github.gavvydizzle.boosters.boost.event.PrisonEnchantsListener;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.*;
import me.github.gavvydizzle.boosters.gui.BoostListMenu;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class BoostManager implements Listener {

    private static final String BOOST_SAVE_FILE_NAME = "boosts.yml";
    private static final int BOOST_COMPLETION_PERIOD_TICKS = 20 * 10;

    private final BoostPlugin instance;
    private final InventoryManager inventoryManager;
    private RepeatingTask completionClock;
    @Getter private final BossBarManager bossBarManager;

    private final Set<Boost> activeBoosts;

    public BoostManager(BoostPlugin instance, InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        instance.getServer().getPluginManager().registerEvents(this, instance);

        // Only load events for loaded dependencies
        // This ensures no ClassNotFound exceptions get plugin enable
        if (BoostType.COMMAND_DROPS.isEnabled()) instance.getServer().getPluginManager().registerEvents(new CommandDropsListener(this), instance);
        if (BoostType.PRISON_ENCHANTS.isEnabled()) instance.getServer().getPluginManager().registerEvents(new PrisonEnchantsListener(this), instance);

        this.instance = instance;
        bossBarManager = new BossBarManager(instance);
        activeBoosts = new HashSet<>();

        reload();
        loadBoosts();
    }

    public void reload() {
        bossBarManager.reload();
    }

    private void loadBoosts() {
        readBoostsFromFile();
        startCompletionClock();
    }

    private void readBoostsFromFile() {
        File file = new File(instance.getDataFolder(), BOOST_SAVE_FILE_NAME);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection boostsSection = config.getConfigurationSection("boosts");
        if (boostsSection == null) return;

        for (String key : boostsSection.getKeys(false)) {
            ConfigurationSection section = boostsSection.getConfigurationSection(key);
            if (section == null) continue;

            long completionMillis = section.getLong("time");
            if (completionMillis - System.currentTimeMillis() <= 0) continue;

            BoostType type = BoostType.get(section.getString("type"));
            if (type == null) {
                continue;
            } else if (!type.isEnabled()) {
                instance.getLogger().warning("A boost was saved with the type " + type.name() + ". This is boost type is disabled " +
                        "because one or more dependencies are missing: " + type.getRequiredPlugins());
                continue;
            }

            BoostTarget target = BoostTarget.parseTarget(section.getConfigurationSection("target"));
            if (target == null) {
                instance.getLogger().warning("Failed to parse target data for boost " + key);
                continue;
            }

            VisualBossBar visualBossBar = bossBarManager.createBoostBossBar(type, section.getString("bossbar", ""));
            if (visualBossBar != null && visualBossBar.getContent().isEmpty()) {
                visualBossBar = null;
            }

            Boost boost = null;

            if (type == BoostType.POTION_EFFECT) {
                PotionEffectType effectType = PotionEffectType.getByName(section.getString("data.effect", "null"));
                if (effectType == null) continue;

                boost = new PotionEffectBoost(target, completionMillis, effectType, Math.max(0, section.getInt("data.amplifier")), visualBossBar);
            } else if (type == BoostType.VANILLA_FISHING) {
                VanillaFishingBoost.WaitType waitType = VanillaFishingBoost.WaitType.get(section.getString("data.type"));
                if (waitType == null) continue;

                new VanillaFishingBoost(target, completionMillis, Math.max(0, section.getDouble("data.multiplier", 1.0)), waitType, visualBossBar);
            }
            else {
                boost = switch (type) {
                    case COMMAND_DROPS ->
                            new CommandDropsBoost(target, completionMillis, Math.max(0, section.getDouble("data.multiplier", 1.0)), visualBossBar);
                    case PRISON_ENCHANTS ->
                            new PrisonEnchantsBoost(target, completionMillis, Math.max(0, section.getDouble("data.multiplier", 1.0)), visualBossBar);
                    case LEVELS, SHOPGUIPLUS_SELL_BOOST ->
                            new TimedPermissionBoost(type, target, completionMillis, section.getString("data.permission"), visualBossBar);
                    default -> null;
                };
            }
            if (boost == null) continue;

            registerBoost(boost);
        }
    }

    /**
     * Since a long tick-based delay may be unreliable, the scheduling for its completion should be handled by a clock.
     * This will attempt to schedule a completion for any boost which should complete within this clock cycle.
     */
    private void startCompletionClock() {
        completionClock = new RepeatingTask(instance, 0, BOOST_COMPLETION_PERIOD_TICKS) {
            @Override
            public void run() {
                purgeCompletedBoosts();
                activeBoosts.forEach(boost -> attemptBoostCompletionSchedule(boost));
            }
        };
    }

    // Acts as the garbage collector for completed boosts
    private void purgeCompletedBoosts() {
        activeBoosts.removeIf(boost -> boost.getMillisRemaining() < 0);
    }

    public void attemptBoostCompletionSchedule(Boost boost) {
        if (boost.getTicksRemaining() <= BOOST_COMPLETION_PERIOD_TICKS) {
            // Handles the edge case where this boost may be scheduled for completion multiple times
            boost.scheduleCompletion();
        }
    }

    /**
     * Register a new boost and start it.
     * @param boost The boost
     * @return True if the boost matched an existing one and false otherwise
     */
    public boolean registerBoost(Boost boost) {
        for (Boost boost1 : activeBoosts) {
            if (boost1.matches(boost)) {
                boost1.addMillis(boost.getMillisRemaining());
                inventoryManager.refreshBoostMenus();
                return true;
            }
        }

        activeBoosts.add(boost);
        boost.start();
        attemptBoostCompletionSchedule(boost);
        inventoryManager.addBoostToMenus(boost);
        return false;
    }

    public void unregisterBoost(Boost boost) {
        activeBoosts.remove(boost);
        inventoryManager.removeBoostFromMenus(boost);
    }

    public void cleanup() {
        if (completionClock != null) completionClock.cancel();

        purgeCompletedBoosts();
        saveBoosts();
        activeBoosts.forEach(Boost::cleanupTasks);
    }

    public void saveBoosts() {
        File file = new File(instance.getDataFolder(), BOOST_SAVE_FILE_NAME);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("boosts", null);

        config.createSection("boosts");
        ConfigurationSection boostsSection = config.getConfigurationSection("boosts");
        assert boostsSection != null;

        activeBoosts.forEach(boost -> boostsSection.createSection(String.valueOf(boost.getId()), boost.serialize()));

        try {
            config.save(file);
        } catch (Exception e) {
            instance.getLogger().log(Level.SEVERE, "Failed to save boosts file: " + file.getName(), e);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        activeBoosts.forEach(boost -> boost.onPlayerJoin(e.getPlayer()));
    }


    public void sendEventToBoosts(Event e) {
        activeBoosts.stream().filter(boost -> boost instanceof EventBoost<?>).forEach(boost -> ((EventBoost<?>) boost).onEvent(e));
    }

    //*** VANILLA EVENT BOOSTS ***//

    @EventHandler(ignoreCancelled = true)
    private void onFish(PlayerFishEvent e) {
        // Quick-fail for unused fishing states
        // Modify this check if other boosts are needed under this event
        if (e.getState() != PlayerFishEvent.State.FISHING) return;

        sendEventToBoosts(e);
    }

    /**
     * Adds all matching boosts to this menu for viewing
     * @param menu The menu to modify
     */
    public void populateNewMenu(BoostListMenu menu) {
        // The targets of menus will only be single player or Global
        for (Boost boost : activeBoosts) {
            if (BoostTarget.overlap(menu.getTarget(), boost.getTarget())) {
                inventoryManager.addBoostToMenu(boost, menu);
            }
        }
    }

}
