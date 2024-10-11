package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.commanddrops.event.AttemptRewardRollEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Map;

public class CommandDropsBoost extends EventBoost<AttemptRewardRollEvent> {

    private final double multiplier;

    public CommandDropsBoost(BoostTarget target, long completionMillis, double multiplier, @Nullable VisualBossBar visualBossBar) {
        super(AttemptRewardRollEvent.class, BoostType.COMMAND_DROPS, target, completionMillis, visualBossBar);
        this.multiplier = multiplier;
    }

    @Override
    public void handleEvent(AttemptRewardRollEvent event) {
        Player player = event.getPlayer();
        if (notValidPlayer(player)) return;

        event.multiplyRollChance(multiplier);
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of("data.multiplier", multiplier);
    }

    @Override
    public boolean doesDataMatch(Boost boost) {
        return boost instanceof CommandDropsBoost b && multiplier == b.multiplier;
    }

    @Override
    public void replaceItemPlaceholders(ItemStack itemStack, DateFormat dateFormat) {
        super.replaceItemPlaceholders(itemStack, dateFormat);

        ItemStackUtils.replacePlaceholders(itemStack, Map.of("{multiplier}", String.valueOf(multiplier)));
    }
}
