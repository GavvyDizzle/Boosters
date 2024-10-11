package me.github.gavvydizzle.boosters.gui;

import com.github.mittenmc.anvilgui.AnvilGUI;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.gui.pages.DisplayItem;
import com.github.mittenmc.serverutils.gui.pages.PagesMenu;
import com.github.mittenmc.serverutils.item.ItemStackBuilder;
import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.List;

@Getter
public class BoostListMenu extends PagesMenu<BoostMenuItem> {

    private final BoostTarget target;
    private final boolean adminOpened;
    private final AnvilGUI.Builder anvilGUI;

    public BoostListMenu(String menuName, BoostTarget target, boolean adminOpened) {
        super(new PagesMenu.PagesMenuBuilder<>(menuName, 6));
        this.target = target;
        this.adminOpened = adminOpened;

        if (adminOpened) {
            this.anvilGUI = new AnvilGUI.Builder()
                    .plugin(BoostPlugin.getInstance())
                    .title("Modify Boost Duration");
        } else {
            this.anvilGUI = null;
        }

        if (adminOpened) {
            addClickableItem(45, new DisplayItem<>(ItemStackBuilder.of(Material.BOOK)
                    .name("&eMenu Help")
                    .lore("&aLeft-click to increase boost duration", "&cRight-click to cancel boost")
                    .build()
            ));
        }
    }

    @Override
    public void onItemClick(InventoryClickEvent e, Player player, BoostMenuItem item) {
        if (!adminOpened) return;

        if (e.getClick() == ClickType.RIGHT) {
            item.getBoost().cancel();
        } else if (e.getClick() == ClickType.LEFT) {


            anvilGUI
                    .itemLeft(ItemStackBuilder.of(Material.PAPER).name("0").lore(
                            "&7Input a duration like 2h15m25s or a timestamp (in seconds)",
                            "",
                            "&7Decrease time by putting a negative duration like -10m30s",
                            "&7or a Unix timestamp which falls before the boost's end time",
                            "",
                            "&fUnix time now = " + (System.currentTimeMillis()/1000),
                            "&fBoost completion time = " + ((item.getBoost().getMillisRemaining() + System.currentTimeMillis())/1000)
                    ).build())
                    .itemRight(item.getMenuItem(player))
                    .onClose(stateSnapshot -> BoostPlugin.getInstance().getInventoryManager().openMenuDelayed(player, this))
                    .onClick((slot, stateSnapshot) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        }

                        // If the boost ended while this menu is open
                        if (item.getBoost().getMillisRemaining() < 0) {
                            player.sendMessage(ChatColor.YELLOW + "This boost has ended! Closing menu");
                            return List.of(AnvilGUI.ResponseAction.close());
                        }

                        boolean negative = stateSnapshot.getText().startsWith("-");
                        String durationString = stateSnapshot.getText();
                        if (negative) {
                            durationString = durationString.substring(1);
                        }

                        // If input starts with '-' then try to reduce time
                        if (negative && !containsLetter(durationString)) {
                            player.sendMessage(ChatColor.YELLOW + "You can only reduce time with a negative duration");
                            return Collections.emptyList();
                        } else if (negative && containsLetter(durationString)) {
                            long decreaseMillis = Boost.parseNegativeMillis(durationString, item.getBoost());
                            item.getBoost().removeMillis(decreaseMillis);

                            // Allow duration to decrease time or cancel boost if time goes negative
                            if (item.getBoost().getMillisRemaining() <= 0) {
                                player.sendMessage(ChatColor.YELLOW + "Updated completion time was in the past. The boost has been cancelled");
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Successfully removed " + Numbers.getTimeFormatted(decreaseMillis/1000) + " from the boost");
                            }

                            return List.of(AnvilGUI.ResponseAction.close());
                        }

                        // Allow time reduction with unix time
                        if (!containsLetter(durationString)) {
                            long newCompletionMillis = Boost.parseCompletionTime(durationString);

                            if (newCompletionMillis <= System.currentTimeMillis()) {
                                player.sendMessage(ChatColor.YELLOW + "This unix time is in the past. Nothing has been done");
                                return Collections.emptyList();
                            }

                            long decreaseMillis = item.getBoost().getMillisRemaining() - (newCompletionMillis - System.currentTimeMillis());
                            if (decreaseMillis > 0) {
                                item.getBoost().removeMillis(decreaseMillis);
                                player.sendMessage(ChatColor.GREEN + "Successfully removed " + Numbers.getTimeFormatted(decreaseMillis/1000) + " from the boost");
                                return List.of(AnvilGUI.ResponseAction.close());
                            }
                        }

                        // Logic for adding time
                        long additionalMillis = Boost.parseAdditionalMillis(durationString, item.getBoost());
                        if (additionalMillis == 0) {
                            player.sendMessage(ChatColor.RED + "You entered the boost's current end time");
                            return Collections.emptyList();
                        }

                        item.getBoost().addMillis(additionalMillis);
                        player.sendMessage(ChatColor.GREEN + "Successfully added " + Numbers.getTimeFormatted(additionalMillis/1000) + " to the boost");
                        return List.of(AnvilGUI.ResponseAction.close());
                    })
                    .open(player);
        }
    }

    private boolean containsLetter(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) return true;
        }
        return false;
    }
}
