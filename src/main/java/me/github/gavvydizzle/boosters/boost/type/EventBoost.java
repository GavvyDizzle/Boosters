package me.github.gavvydizzle.boosters.boost.type;

import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public abstract class EventBoost<E extends Event> extends Boost {

    private final Class<E> eventType;

    public EventBoost(Class<E> eventType, BoostType type, BoostTarget target, long completionMillis, @Nullable VisualBossBar visualBossBar) {
        super(type, target, completionMillis, visualBossBar);
        this.eventType = eventType;
    }

    /**
     * Passes on any event to {@link #handleEvent(Event)} if it is of type {@link E}
     * @param e The event
     */
    public void onEvent(Event e) {
        if (eventType.isInstance(e)) {
            handleEvent(eventType.cast(e));
        }
    }

    /**
     * Called when an instance of the desired event is fired.
     * @param event The event
     */
    public abstract void handleEvent(E event);

    @Override
    public void finish() {
        // Not needed for this boost type
    }
}
