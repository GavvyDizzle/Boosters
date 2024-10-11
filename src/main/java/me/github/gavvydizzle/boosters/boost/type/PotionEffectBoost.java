package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.RepeatingTask;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Map;

public class PotionEffectBoost extends Boost {

    // There is no way to remove an effect safely on boost cancel.
    // Keep the clock delta on the smaller side to keep effect linger small.
    private static final int EFFECT_DURATION_TICKS = 20 * 8;
    private static final int EFFECT_APPLY_PERIOD_TICKS = 20 * 5;

    private final PotionEffect effect;
    private RepeatingTask task;

    public PotionEffectBoost(BoostTarget target, long completionMillis, PotionEffectType effectType, int amplifier, @Nullable VisualBossBar visualBossBar) {
        super(BoostType.POTION_EFFECT, target, completionMillis, visualBossBar);
        effect = new PotionEffect(effectType, EFFECT_DURATION_TICKS, amplifier);
    }

    private void startEffectClock() {
        task = new RepeatingTask(BoostPlugin.getInstance(), 0, EFFECT_APPLY_PERIOD_TICKS) {
            @Override
            public void run() {
                applyEffect();
            }
        };
    }

    private void applyEffect() {
        if (getTicksRemaining() < EFFECT_DURATION_TICKS) {
            PotionEffect shortened = new PotionEffect(effect.getType(), (int) getTicksRemaining(), effect.getAmplifier());
            super.getOnlinePlayers().forEach(player -> player.addPotionEffect(shortened));
        } else {
            super.getOnlinePlayers().forEach(player -> player.addPotionEffect(effect));
        }
    }

    private void applyEffect(Player player) {
        if (getTicksRemaining() < EFFECT_DURATION_TICKS) {
            PotionEffect shortened = new PotionEffect(effect.getType(), (int) getTicksRemaining(), effect.getAmplifier());
            player.addPotionEffect(shortened);
        } else {
            player.addPotionEffect(effect);
        }
    }

    @Override
    protected void onTargetJoin(Player player) {
        super.onTargetJoin(player);
        applyEffect(player);
    }

    @Override
    public void start() {
        super.start();
        startEffectClock();
    }

    @Override
    public void finish() {
        if (task != null) task.cancel();
    }

    @Override
    public void onTimeIncrease(long increaseMillis) {
        super.onTimeIncrease(increaseMillis);
        applyEffect();
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of(
                "data.effect", effect.getType().getName(),
                "data.amplifier", effect.getAmplifier()
        );
    }

    @Override
    public boolean doesDataMatch(Boost boost) {
        return boost instanceof PotionEffectBoost b && effect.getType() == b.effect.getType() && effect.getAmplifier() == b.effect.getAmplifier();
    }

    @Override
    public void replaceItemPlaceholders(ItemStack itemStack, DateFormat dateFormat) {
        super.replaceItemPlaceholders(itemStack, dateFormat);

        ItemStackUtils.replacePlaceholders(itemStack, Map.of(
                "{effect}", capitalize(effect.getType().getKey().getKey()),
                "{amplifier}", String.valueOf(effect.getAmplifier()+1),
                "{amplifier_roman}", Numbers.toRomanNumeral(effect.getAmplifier()+1)
        ));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String s : str.split("_")) {
            sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    @Override
    public void cleanupTasks() {
        super.cleanupTasks();
        if (task != null) task.cancel();
    }
}
