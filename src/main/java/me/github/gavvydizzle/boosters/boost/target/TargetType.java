package me.github.gavvydizzle.boosters.boost.target;

import org.jetbrains.annotations.Nullable;

public enum TargetType {
    GLOBAL,
    GROUP,
    PLAYER;

    @Nullable
    public static TargetType get(String str) {
        for (TargetType type : values()) {
            if (type.name().equalsIgnoreCase(str)) return type;
        }
        return null;
    }
}