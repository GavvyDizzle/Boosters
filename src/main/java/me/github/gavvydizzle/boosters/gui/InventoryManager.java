package me.github.gavvydizzle.boosters.gui;

import com.github.mittenmc.lib.folialib.wrapper.task.WrappedTask;
import com.github.mittenmc.serverutils.ConfigUtils;
import com.github.mittenmc.serverutils.gui.MenuManager;
import com.github.mittenmc.serverutils.gui.pages.PagesMenu;
import com.github.mittenmc.serverutils.item.ItemStackBuilder;
import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.target.GlobalTarget;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class InventoryManager extends MenuManager {

    private static final int MENU_UPDATE_TICKS = 20;

    private final BoostPlugin instance;
    private WrappedTask menuRefreshTask;
    private final Map<UUID, BoostListMenu> activeListMenus;

    private DateFormat dateFormat;
    private final Map<BoostType, ItemStack> playerTemplateItems;
    private final Map<BoostType, ItemStack> adminTemplateItems;

    public InventoryManager(BoostPlugin instance) {
        super(instance);
        this.instance = instance;

        activeListMenus = new HashMap<>();
        playerTemplateItems = new HashMap<>();
        adminTemplateItems = new HashMap<>();

        startUpdateClock();

        reload();
    }

    public void reload() {
        closeAllMenus();

        playerTemplateItems.clear();
        adminTemplateItems.clear();

        FileConfiguration config = instance.getConfigManager().get("menus");
        if (config == null) return;

        config.addDefault("time.date_format", "d MMM yyyy h:mm:ssa z");
        config.addDefault("time.time_zone", "America/New_York");

        dateFormat = new SimpleDateFormat(config.getString("time.date_format", "d MMM yyyy h:mm:ssa z"));
        TimeZone timeZone = TimeZone.getTimeZone(config.getString("time.time_zone", TimeZone.getDefault().getID()));
        if (timeZone != null) dateFormat.setTimeZone(timeZone);

        config.addDefault("lore.player", List.of("&7Time Remaining: &b{time_formatted}", "&7End Date: &b{end_date}"));
        config.addDefault("lore.admin", List.of("&7Time Remaining: &b{time_formatted}", "&7End Date: &b{end_date}", "&7Targets: &e{targets}"));

        List<String> playerLore = config.getStringList("lore.player");
        List<String> adminLore = config.getStringList("lore.admin");

        if (!config.isConfigurationSection("type")) config.createSection("type");
        ConfigurationSection typeSection = config.getConfigurationSection("type");
        assert typeSection != null;

        for (BoostType type : BoostType.values()) {
            if (!type.isEnabled()) continue;

            if (!typeSection.isConfigurationSection(type.name())) typeSection.createSection(type.name());
            ConfigurationSection section = typeSection.getConfigurationSection(type.name());
            assert section != null;

            section.addDefault("material", type.defaultMenuMaterial.name());
            section.addDefault("default_name", type.name() + " Boost");
            section.addDefault("prefix_lore", List.of());

            ItemStack template = ItemStackBuilder.of(ConfigUtils.getMaterial(section.getString("material"), Material.DIRT))
                    .name(section.getString("default_name"))
                    .lore(section.getStringList("prefix_lore"))
                    .build();

            playerTemplateItems.put(type, ItemStackBuilder.of(template.clone()).lore(playerLore).build());
            adminTemplateItems.put(type, ItemStackBuilder.of(template.clone()).lore(adminLore).build());
        }
    }

    /**
     * Updates all boost PagesMenus once a second.
     * This operation is potentially strenuous on the server!
     */
    private void startUpdateClock() {
        menuRefreshTask = instance.getFoliaLib().getScheduler().runTimerAsync(this::refreshBoostMenus, MENU_UPDATE_TICKS, MENU_UPDATE_TICKS);
    }

    public void cleanup() {
        if (menuRefreshTask != null) menuRefreshTask.cancel();
    }

    public void refreshBoostMenus() {
        activeListMenus.values().forEach(PagesMenu::refresh);
    }

    /**
     * Opens the menu for this player given the target selector.
     * A new instance is opened for every player.
     * @param viewer The player
     * @param target The target to filter for
     * @param adminOpened If the admin command was used to open the menu
     */
    public void openMenu(Player viewer, BoostTarget target, boolean adminOpened) {
        // Add the single player's (target) name to the inventory name
        String menuName = "Active Boosts";
        if (target instanceof PlayerTarget pt) {
            Optional<? extends Player> opt = pt.getOnlinePlayers().stream().findFirst();
            if (opt.isPresent()) {
                menuName += " - " + opt.get().getName();
            }
        } else if (target instanceof GlobalTarget) {
            menuName += " - Global";
        }

        BoostListMenu menu = new BoostListMenu(menuName, target, adminOpened);
        instance.getBoostManager().populateNewMenu(menu);
        activeListMenus.put(viewer.getUniqueId(), menu);
        super.openMenu(viewer, menu);
    }

    public void addBoostToMenu(Boost boost, BoostListMenu menu) {
        ItemStack playerItem = playerTemplateItems.get(boost.getType());
        ItemStack adminItem = adminTemplateItems.get(boost.getType());

        if (BoostTarget.overlap(menu.getTarget(), boost.getTarget())) {
            menu.addItem(new BoostMenuItem(this, boost, menu.isAdminOpened() ? adminItem : playerItem));
        }
    }

    public void addBoostToMenus(Boost boost) {
        ItemStack playerItem = playerTemplateItems.get(boost.getType());
        ItemStack adminItem = adminTemplateItems.get(boost.getType());

        activeListMenus.forEach((uuid, menu) -> {
            if (BoostTarget.overlap(menu.getTarget(), boost.getTarget())) {
                menu.addItem(new BoostMenuItem(this, boost, menu.isAdminOpened() ? adminItem : playerItem));
            }
        });
    }

    public void removeBoostFromMenus(Boost boost) {
        activeListMenus.forEach((uuid, menu) -> {
            if (BoostTarget.overlap(menu.getTarget(), boost.getTarget())) {
                menu.removeItem(new BoostMenuItem(this, boost, null));
            }
        });
    }
}
