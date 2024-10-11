package me.github.gavvydizzle.boosters.boost.target;

import com.github.mittenmc.serverutils.PlayerNameCache;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class PlayerTarget extends BoostTarget {

    private final Set<UUID> uuids;
    private String targetString;

    public PlayerTarget(Player player) {
        this(List.of(player));
    }

    public PlayerTarget(List<OfflinePlayer> players) {
        super(TargetType.PLAYER);

        this.uuids = new HashSet<>();
        this.uuids.addAll(players.stream().map(OfflinePlayer::getUniqueId).toList());
    }

    public PlayerTarget(Set<UUID> uuids) {
        super(TargetType.PLAYER);

        this.uuids = uuids;
    }

    @Override
    public boolean isTarget(UUID uuid) {
        return uuids.contains(uuid);
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of("target.list", uuids.stream().map(UUID::toString).toList());
    }

    @Override
    public String getStringTargets() {
        if (targetString == null) {
            StringBuilder sb = new StringBuilder();

            for (UUID uuid : uuids) {
                sb.append(PlayerNameCache.get(uuid)).append(",");
            }
            sb.deleteCharAt(sb.length()-1);

            targetString = sb.toString();
        }

        return targetString;
    }

    @Override
    public boolean doesDataMatch(BoostTarget target) {
        return target instanceof PlayerTarget t && uuids.equals(t.uuids);
    }
}
