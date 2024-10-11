package me.github.gavvydizzle.boosters.boost.type;

import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public enum BoostType {
    COMMAND_DROPS(List.of("CommandDrops"), Material.BUNDLE),
    LEVELS(List.of("Levels", "LuckPerms"), Material.EXPERIENCE_BOTTLE),
    POTION_EFFECT(Material.BEACON),
    PRISON_ENCHANTS(List.of("PrisonEnchants"), Material.ENCHANTING_TABLE),
    SHOPGUIPLUS_SELL_BOOST(List.of("ShopGUIPlus", "LuckPerms"), Material.SUNFLOWER),
    VANILLA_FISHING(Material.FISHING_ROD);

    private boolean enabled;
    private final List<String> requiredPlugins;
    public final Material defaultMenuMaterial;

    BoostType(Material defaultMenuMaterial) {
        this.enabled = false;
        this.requiredPlugins = List.of();
        this.defaultMenuMaterial = defaultMenuMaterial;
    }

    BoostType(List<String> requiredPlugins, Material defaultMenuMaterial) {
        this.requiredPlugins = requiredPlugins;
        this.defaultMenuMaterial = defaultMenuMaterial;
    }

    private boolean hasAllDependenciesLoaded() {
        if (requiredPlugins.stream().allMatch(name -> Bukkit.getServer().getPluginManager().isPluginEnabled(name))) {
            BoostPlugin.getInstance().getLogger().info("Loaded boost type " + this.name());
            return true;
        }
        return false;
    }

    public static void setEnabled() {
        for (BoostType type : values()) {
            type.enabled = type.hasAllDependenciesLoaded();
        }
    }

    @Nullable
    public static BoostType get(String str) {
        for (BoostType type : values()) {
            if (type.name().equalsIgnoreCase(str)) return type;
        }
        return null;
    }
}
