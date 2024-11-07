package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.target.GlobalTarget;
import me.github.gavvydizzle.boosters.boost.target.GroupTarget;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * A boost which gives players a temporary permission through LuckPerms
 * @link <a href="https://luckperms.net/wiki/Permission-Commands">LuckPerms Permission Commands</a>
 */
public class TimedPermissionBoost extends Boost {

    private static final String GIVE_PLAYER_PERMISSION = "lp user {player} permission settemp {perm} true {time} accumulate";
    private static final String GIVE_ALL_PERMISSION = "lp group default permission settemp {perm} true {time} accumulate";
    private static final String GIVE_GROUP_PERMISSION = "lp group {group} permission settemp {perm} true {time} accumulate";
    private static final String REMOVE_PLAYER_PERMISSION = "lp user {player} permission unsettemp {perm}";
    private static final String REMOVE_ALL_PERMISSION = "lp group default permission unsettemp {perm}";
    private static final String REMOVE_GROUP_PERMISSION = "lp group {group} permission unsettemp {perm}";

    private final String permission;

    public TimedPermissionBoost(BoostType type, BoostTarget target, long completionMillis, String permission, @Nullable VisualBossBar visualBossBar) {
        super(type, target, completionMillis, visualBossBar);
        this.permission = permission;
    }

    @Override
    public void start() {
        super.start();
        increasePermissionTime(super.getMillisRemaining());
    }

    /**
     * Increase the time of this boost without impacting the bossbar
     * @param increasedMillis The time increase in milliseconds
     */
    private void increasePermissionTime(long increasedMillis) {
        if (increasedMillis < 1000) return;

        String increasedSecondsString = Numbers.getTimeFormatted(Math.max(1, increasedMillis / 1000));

        BoostTarget target = super.getTarget();

        if (target instanceof GlobalTarget) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), GIVE_ALL_PERMISSION
                    .replace("{perm}", permission)
                    .replace("{time}", increasedSecondsString)
            );
        }
        else if (target instanceof PlayerTarget players) {
            String temp = GIVE_PLAYER_PERMISSION
                    .replace("{perm}", permission)
                    .replace("{time}", increasedSecondsString
                    );

            for (UUID uuid : players.getUuids()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp.replace("{player}", uuid.toString()));
            }
        }
        else if (target instanceof GroupTarget groupTarget) {
            String temp = GIVE_GROUP_PERMISSION
                    .replace("{perm}", permission)
                    .replace("{time}", String.valueOf(increasedSecondsString)
                    );

            for (Group group : groupTarget.getGroups()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp.replace("{group}", group.getName()));
            }
        }
    }

    @Override
    public void onTimeIncrease(long increasedMillis) {
        super.onTimeIncrease(increasedMillis);
        increasePermissionTime(increasedMillis);
    }

    // As long as the boost is handled correctly, these commands are unnecessary when the boost completes normally.
    // Since boost cancellation comes through here too, it will be left. It will create extra console logs.
    @Override
    public void finish() {
        BoostTarget target = super.getTarget();

        if (target instanceof GlobalTarget) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), REMOVE_ALL_PERMISSION
                    .replace("{perm}", permission)
            );
        }
        else if (target instanceof PlayerTarget players) {
            String temp = REMOVE_PLAYER_PERMISSION.replace("{perm}", permission);

            for (UUID uuid : players.getUuids()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp.replace("{player}", uuid.toString()));
            }
        }
        else if (target instanceof GroupTarget groupTarget) {
            String temp = REMOVE_GROUP_PERMISSION.replace("{perm}", permission);

            for (Group group : groupTarget.getGroups()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp.replace("{group}", group.getName()));
            }
        }
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of("data.permission", permission);
    }

    @Override
    public boolean doesDataMatch(Boost boost) {
        return boost instanceof TimedPermissionBoost b && permission.equals(b.permission);
    }
}
