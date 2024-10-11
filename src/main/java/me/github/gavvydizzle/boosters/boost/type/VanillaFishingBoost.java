package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Map;

public class VanillaFishingBoost extends EventBoost<PlayerFishEvent> {
    
    public enum WaitType {
        ALL, // Both types
        ANIMATION_TIME, // The time it takes for the fish to hook after the bubble animation begins
        WAIT_TIME; // The time it takes for the bubble animation to begin

        @Nullable
        public static WaitType get(String str) {
            for (WaitType waitType : values()) {
                if (waitType.name().equalsIgnoreCase(str)) return waitType;
            }
            return null;
        }
    }

    private static final int LURE_TICKS_PER_LEVEL = 20 * 5;
    private static final int MIN_WAIT_TICKS = 1;

    private final double multiplier;
    private final WaitType waitType;

    public VanillaFishingBoost(BoostTarget target, long completionMillis, double multiplier, WaitType waitType, @Nullable VisualBossBar visualBossBar) {
        super(PlayerFishEvent.class, BoostType.VANILLA_FISHING, target, completionMillis, visualBossBar);
        this.multiplier = Math.max(multiplier, 1e-6); // Keep the multiplier a non-zero value
        this.waitType = waitType;
    }

    @Override
    public void handleEvent(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) return;

        Player player = event.getPlayer();
        if (notValidPlayer(player)) return;

        // Yes, I agree this statement seems counter-intuitive
        if (waitType == WaitType.WAIT_TIME || waitType == WaitType.ALL) {
            applyLureEnchantEffects(event);
        }

        // Decrease the min and max wait times by the multiplier
        FishHook hook = event.getHook();
        switch (waitType) {
            case ANIMATION_TIME -> {
                int minLure = (int) (hook.getMinLureTime() / multiplier);
                int maxLure = (int) (hook.getMaxLureTime() / multiplier);
                hook.setLureTime(Math.max(MIN_WAIT_TICKS, minLure), Math.max(MIN_WAIT_TICKS, maxLure));
            }
            case WAIT_TIME -> {
                int minWait = (int) (hook.getMinWaitTime() / multiplier);
                int maxWait = (int) (hook.getMaxWaitTime() / multiplier);
                hook.setWaitTime(Math.max(MIN_WAIT_TICKS, minWait), Math.max(MIN_WAIT_TICKS, maxWait));
            }
            case ALL -> {
                int minLure = (int) (hook.getMinLureTime() / multiplier);
                int maxLure = (int) (hook.getMaxLureTime() / multiplier);
                hook.setLureTime(Math.max(MIN_WAIT_TICKS, minLure), Math.max(MIN_WAIT_TICKS, maxLure));

                int minWait = (int) (hook.getMinWaitTime() / multiplier);
                int maxWait = (int) (hook.getMaxWaitTime() / multiplier);
                hook.setWaitTime(Math.max(MIN_WAIT_TICKS, minWait), Math.max(MIN_WAIT_TICKS, maxWait));
            }
        }
    }

    /**
     * Handle lure here to allow catches with the boost time reduction
     * Lure dumbly subtracts time. If the time goes negative, no fish can appear.
     * @param event The fishing event to modify
     */
    private void applyLureEnchantEffects(PlayerFishEvent event) {
        event.getHook().setApplyLure(false);

        ItemStack rod = event.getPlayer().getActiveItem();
        if (rod.getType() != Material.FISHING_ROD) return;

        int lureLevel = rod.getEnchantmentLevel(Enchantment.LURE);
        if (lureLevel <= 0) return;

        int min = event.getHook().getMinWaitTime() - (lureLevel * LURE_TICKS_PER_LEVEL);
        int max = event.getHook().getMaxWaitTime() - (lureLevel * LURE_TICKS_PER_LEVEL);
        event.getHook().setWaitTime(Math.max(MIN_WAIT_TICKS, min), Math.max(MIN_WAIT_TICKS, max));
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of(
                "data.multiplier", multiplier,
                "data.waitType", waitType.name()
        );
    }

    @Override
    public boolean doesDataMatch(Boost boost) {
        return boost instanceof VanillaFishingBoost b && multiplier == b.multiplier && waitType == b.waitType;
    }

    @Override
    public void replaceItemPlaceholders(ItemStack itemStack, DateFormat dateFormat) {
        super.replaceItemPlaceholders(itemStack, dateFormat);

        ItemStackUtils.replacePlaceholders(itemStack, Map.of("{multiplier}", String.valueOf(multiplier)));
    }
}
