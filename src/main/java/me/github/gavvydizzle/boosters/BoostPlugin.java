package me.github.gavvydizzle.boosters;

import com.github.mittenmc.serverutils.ConfigManager;
import com.github.mittenmc.serverutils.autosave.AutoSaver;
import lombok.Getter;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.command.AdminCommandManager;
import me.github.gavvydizzle.boosters.command.PlayerCommandManager;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BoostPlugin extends JavaPlugin {

    @Getter private static BoostPlugin instance;
    @Getter private InventoryManager inventoryManager;
    @Getter private BoostManager boostManager;
    @Getter private ConfigManager configManager;
    private AutoSaver autoSaver;

    @Nullable @Getter private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        instance = this;
        BoostType.setEnabled();

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        configManager = new ConfigManager(this, Set.of("bossbar", "menus"));

        inventoryManager = new InventoryManager(this);
        boostManager = new BoostManager(this, inventoryManager);

        new AdminCommandManager(getCommand("boostadmin"), this, boostManager, inventoryManager);
        new PlayerCommandManager(getCommand("boost"), inventoryManager);

        autoSaver = new AutoSaver(this, AutoSaver.TimeUnit.ONE_MINUTE) {
            @Override
            public void save() {
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    if (boostManager != null) boostManager.saveBoosts();
                });
            }
        };

        configManager.saveAll();
    }

    @Override
    public void onDisable() {
        if (boostManager != null) boostManager.cleanup();
        if (inventoryManager != null) {
            inventoryManager.cleanup();
            inventoryManager.closeAllMenus();
        }
        if (autoSaver != null) autoSaver.cancel();

        HandlerList.unregisterAll(this);
    }
}
