package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.wax.prisonenchants.events.AttemptEnchantActivationEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Map;

public class PrisonEnchantsBoost extends EventBoost<AttemptEnchantActivationEvent> {

    private final double multiplier;

    public PrisonEnchantsBoost(BoostTarget target, long completionMillis, double multiplier, @Nullable VisualBossBar visualBossBar) {
        super(AttemptEnchantActivationEvent.class, BoostType.PRISON_ENCHANTS, target, completionMillis, visualBossBar);
        this.multiplier = multiplier;
    }

    @Override
    public void handleEvent(AttemptEnchantActivationEvent event) {
        Player player = event.getPlayer();
        if (notValidPlayer(player)) return;

        event.multiplyActivationChance(multiplier);
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of("data.multiplier", multiplier);
    }

    @Override
    public boolean doesDataMatch(Boost boost) {
        return boost instanceof PrisonEnchantsBoost b && multiplier == b.multiplier;
    }

    @Override
    public void replaceItemPlaceholders(ItemStack itemStack, DateFormat dateFormat) {
        super.replaceItemPlaceholders(itemStack, dateFormat);

        ItemStackUtils.replacePlaceholders(itemStack, Map.of("{multiplier}", String.valueOf(multiplier)));
    }
}
