package me.github.gavvydizzle.boosters.boost;

import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class BossBarManager {

    private final BoostPlugin instance;
    private final EnumMap<BoostType, VisualBossBar> map;

    public BossBarManager(BoostPlugin instance) {
        this.instance = instance;
        map = new EnumMap<>(BoostType.class);
    }

    protected void reload() {
        FileConfiguration config = instance.getConfigManager().get("bossbar");
        if (config == null) return;

        if (!config.isConfigurationSection("type")) config.createSection("type");
        ConfigurationSection typeSection = config.getConfigurationSection("type");
        assert typeSection != null;

        map.clear();
        for (BoostType type : BoostType.values()) {
            if (!type.isEnabled()) continue;

            if (!typeSection.isConfigurationSection(type.name())) typeSection.createSection(type.name());
            ConfigurationSection section = typeSection.getConfigurationSection(type.name());
            assert section != null;

            VisualBossBar.addDefaults(section);
            map.put(type, VisualBossBar.deserialize(instance, section));
        }
    }

    @Nullable
    public VisualBossBar createBoostBossBar(BoostType type, String content) {
        VisualBossBar template = map.get(type);
        if (template == null || content.isEmpty()) return null;

        return template.clone(content);
    }
}
