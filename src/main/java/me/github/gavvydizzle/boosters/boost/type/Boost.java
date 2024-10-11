package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.bossbar.BossBarBuilder;
import com.github.mittenmc.serverutils.bossbar.TimedBossBar;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import com.github.mittenmc.serverutils.item.ItemStackBuilder;
import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.*;

/**
 * Defines a boost construct which runs for a set amount of time.
 */
public abstract class Boost implements Comparable<Boost> {

    private static int ID_COUNTER = 1;

    @Getter private final int id;
    @Getter private final BoostType type;
    @Getter private final BoostTarget target;
    protected long completionMillis;
    private int taskID;

    @Nullable private final VisualBossBar visualBossBar;
    @Nullable private TimedBossBar bossBar;

    public Boost(BoostType type, BoostTarget target, long completionMillis, @Nullable VisualBossBar visualBossBar) {
        this.id = ID_COUNTER++;
        this.type = type;
        this.target = target;
        this.completionMillis = completionMillis;
        this.taskID = -1;
        this.visualBossBar = visualBossBar;
    }

    /**
     * Parses a time context from a given string.
     * <p>
     * The method handles two types of inputs:
     * <ul>
     *   <li>If the input string is in the format "2h15m25s", it will be interpreted as a duration, and the method will return the current time plus the parsed duration.</li>
     *   <li>Otherwise, the input is treated as a Unix timestamp. If parsing fails, the method returns -1.</li>
     * </ul>
     * </p>
     *
     * @param str The input string representing either a duration or a Unix timestamp.
     * @return The corresponding Unix time in milliseconds, or -1 if the input is invalid.
     */
    public static long parseCompletionTime(String str) {
        long epoch;
        try {
            epoch = Math.max(-1L, Long.parseLong(str) * 1000);
        } catch (Exception ignored) {
            epoch = System.currentTimeMillis() + Numbers.parseSecondsLong(str) * 1000;
        }
        return epoch;
    }

    /**
     * Parses a time context from a given string.
     * <p>
     * The method handles two types of inputs:
     * <ul>
     *   <li>If the input string is in the format "2h15m25s", it will be interpreted as a duration, and the method will return the parsed duration.</li>
     *   <li>Otherwise, the input is treated as a Unix timestamp. If parsing fails, the method returns -1.</li>
     * </ul>
     * </p>
     *
     * @param str The input string representing either a duration or a Unix timestamp.
     * @param boost The boost to reference for its completion time
     * @return The milliseconds to add to this boost, or -1 if the input is invalid.
     */
    public static long parseAdditionalMillis(String str, Boost boost) {
        try {
            return Math.max(-1L, Long.parseLong(str) * 1000 - boost.completionMillis);
        } catch (Exception ignored) {
            return Numbers.parseSecondsLong(str) * 1000L;
        }
    }

    /**
     * Parses a time context from a given string.
     * <p>
     * The method handles two types of inputs:
     * <ul>
     *   <li>If the input string is in the format "2h15m25s", it will be interpreted as a duration, and the method will return the parsed duration.</li>
     *   <li>Otherwise, the input is treated as a Unix timestamp. If parsing fails, the method returns -1.</li>
     * </ul>
     * </p>
     *
     * @param str The input string representing either a duration or a Unix timestamp.
     * @param boost The boost to reference for its completion time
     * @return The milliseconds to remove from this boost, or 1 if the input is invalid.
     */
    public static long parseNegativeMillis(String str, Boost boost) {
        try {
            return Math.min(1L, boost.completionMillis * 1000 - Long.parseLong(str));
        } catch (Exception ignored) {
            return Numbers.parseSecondsLong(str) * 1000L;
        }
    }

    /**
     * Since a long tick-based delay may be unreliable, the scheduling for its
     * completion is handled by a global timer with a much smaller clock cycle.
     */
    public void scheduleCompletion() {
        // Cancel any previously scheduled cleanup task
        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);

        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(BoostPlugin.getInstance(), () -> {
            finish();
            removeBossBar();
            BoostPlugin.getInstance().getBoostManager().unregisterBoost(this);
        }, getTicksRemaining());
    }

    /**
     * Immediately stop this boost.
     */
    public void cancel() {
        // Cancel any previously scheduled cleanup task
        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);

        finish();
        removeBossBar();
        BoostPlugin.getInstance().getBoostManager().unregisterBoost(this);
    }

    /**
     * Cancel any tasks handled by this boost.
     * This should only be called on plugin disable.
     */
    public void cleanupTasks() {
        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);
    }

    /**
     * Start this boost for all targets
     */
    public void start() {
        this.bossBar = visualBossBar == null ? null : new TimedBossBar(new BossBarBuilder(target.getOnlinePlayers(), visualBossBar).build(), (int) getSecondsRemaining());
    }

    /**
     * Called when the time remaining on this boost has increased
     * @param increaseMillis The amount of time increased in milliseconds
     */
    public void onTimeIncrease(long increaseMillis) {
        if (bossBar != null) bossBar.addTime((int) (increaseMillis/1000));

        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);
        BoostPlugin.getInstance().getBoostManager().attemptBoostCompletionSchedule(this);
    }

    /**
     * Called when the time remaining on this boost has decreased
     * @param decreaseMillis The amount of time decreased in milliseconds
     */
    public void onTimeDecrease(long decreaseMillis) {
        if (getMillisRemaining() <= 0) {
            cancel();
            return;
        }

        if (bossBar != null) bossBar.removeTime((int) (decreaseMillis/1000));

        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);
        BoostPlugin.getInstance().getBoostManager().attemptBoostCompletionSchedule(this);
    }

    /**
     * Called when the time for this boost has expired
     * or when the plugin/server disables
     */
    public abstract void finish();

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        map.put("time", completionMillis);
        map.put("target.type", target.getType().name());
        map.putAll(target.serializeData());
        map.put("bossbar", bossBar == null ? "" : bossBar.getInitialName());
        map.putAll(serializeData());

        return map;
    }

    public abstract Map<String, Object> serializeData();

    /**
     * Called when a player joins the server
     * @param player The player
     */
    public void onPlayerJoin(Player player) {
        if (validPlayer(player)) onTargetJoin(player);
    }

    /**
     * Called when a valid target player joins the server.
     * @param player The player
     */
    protected void onTargetJoin(Player player) {
        if (bossBar != null) bossBar.addViewer(player);
    }

    /**
     * Attempts to add the player as a viewer to this boost's bossbar.
     * If this boost had no bossbar or the player is not a target of
     * this boost, nothing will happen
     * @param player The player
     */
    private void showBossBar(Player player) {
        if (bossBar != null && validPlayer(player)) bossBar.addViewer(player);
    }

    private void removeBossBar() {
        if (bossBar != null) bossBar.delete();
    }

    public void addMillis(long millis) {
        if (millis <= 0) return;

        completionMillis += millis;
        onTimeIncrease(millis);
    }

    public void addTicks(int ticks) {
        addMillis(ticks * 50L);
    }

    public void addSeconds(int seconds) {
        addMillis(seconds * 1000L);
    }

    public void removeMillis(long millis) {
        if (millis <= 0) return;

        completionMillis -= millis;
        onTimeDecrease(millis);
    }

    public long getMillisRemaining() {
        return completionMillis - System.currentTimeMillis();
    }

    public long getTicksRemaining() {
        return (completionMillis - System.currentTimeMillis()) / 50;
    }

    public long getSecondsRemaining() {
        return (completionMillis - System.currentTimeMillis()) / 1000;
    }

    public boolean notValidPlayer(UUID uuid) {
        return !target.isTarget(uuid);
    }

    public boolean notValidPlayer(Player player) {
        return !target.isTarget(player);
    }

    public boolean validPlayer(UUID uuid) {
        return target.isTarget(uuid);
    }

    public boolean validPlayer(Player player) {
        return target.isTarget(player);
    }

    public Collection<? extends Player> getOnlinePlayers() {
        return target.getOnlinePlayers();
    }

    /**
     * Determines if this matches the type, target, and data of another.
     * @param boost The boost
     * @return If these boosts match exactly
     */
    public boolean matches(Boost boost) {
        return this.type == boost.type && this.target.matches(boost.target) && doesDataMatch(boost);
    }

    public abstract boolean doesDataMatch(Boost boost);

    /**
     * Replaces any item placeholders for this boost type.
     * @param itemStack The item to modify
     * @param dateFormat The date formatter
     */
    public void replaceItemPlaceholders(ItemStack itemStack, DateFormat dateFormat) {
        // Replace the name of the template item to match the bossbar if one is active
        if (visualBossBar != null) {
            itemStack = ItemStackBuilder.of(itemStack).name(visualBossBar.getContent()).build();
        }

        ItemStackUtils.replacePlaceholders(itemStack, Map.of(
                "{time}", String.valueOf(getSecondsRemaining()),
                "{time_formatted}", Numbers.getTimeFormatted(getSecondsRemaining()),
                "{end_date}", dateFormat.format(new Date(completionMillis)),
                "{targets}", target.toString()
        ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Boost boost && this.id == boost.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public int compareTo(@NotNull Boost o) {
        return Integer.compare(this.id, o.id);
    }
}
