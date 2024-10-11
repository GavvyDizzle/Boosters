package me.github.gavvydizzle.boosters.boost.target;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class GlobalTarget extends BoostTarget {

    public GlobalTarget() {
        super(TargetType.GLOBAL);
    }

    @Override
    public boolean isTarget(UUID uuid) {
        return true;
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of();
    }

    @Override
    public boolean doesDataMatch(BoostTarget target) {
        return target instanceof GlobalTarget;
    }

    @Override
    public String getStringTargets() {
        return "Global";
    }
}
