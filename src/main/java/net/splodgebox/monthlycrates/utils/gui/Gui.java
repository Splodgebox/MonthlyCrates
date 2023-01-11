package net.splodgebox.monthlycrates.utils.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.splodgebox.monthlycrates.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Gui {

    @Getter
    private static Set<Gui> guis = Sets.newConcurrentHashSet();
    @Getter
    private Inventory inventory;

    @Getter
    private Map<Integer, ClickAction> actions;
    private List<GuiItem<ItemStack, ClickAction>> guiItems;

    @Getter
    private CloseAction closeAction;

    private int current = 0;
    private int currentAdd = 0;

    /**
     * Create a gui with a name and the number of rows in the menu
     * @param name Name of the menu
     * @param rows Amount of rows in the menu
     */
    public Gui(String name, int rows) {
        inventory = Bukkit.createInventory(null, rows * 9, Chat.color(name));
        actions = Maps.newHashMap();
        guiItems = Lists.newArrayList();
        guis.add(this);
    }

    /**
     * Create a gui with a name and the number of slots
     * @param name The name of the menu
     * @param slots The amount of slots in the menu
     * @param dummy dummy bool dw about this!
     */
    public Gui(String name, int slots, boolean dummy) {
        inventory = Bukkit.createInventory(null, slots, Chat.color(name));
        actions = Maps.newHashMap();
        guiItems = Lists.newArrayList();
        guis.add(this);
    }

    /**
     * Create a gui with a name and an inventory type
     * @param name Name of the menu
     * @param type The inventory type of the menu
     */
    public Gui(String name, InventoryType type) {
        inventory = Bukkit.createInventory(null, type, Chat.color(name));
        actions = Maps.newHashMap();
        guiItems = Lists.newArrayList();
        guis.add(this);
    }

    public void addItem(ItemStack item, ClickAction clickAction) {
        inventory.setItem(currentAdd, item);
        actions.put(currentAdd, clickAction);

        currentAdd++;
    }

    public void setItem(int slot, ItemStack item, ClickAction clickAction) {
        inventory.setItem(slot, item);
        actions.put(slot, clickAction);
    }

    public void setItems(GuiItem<ItemStack, ClickAction>... items) {
        for (GuiItem<ItemStack, ClickAction> i : items) {
            inventory.setItem(current, i.getA());
            actions.put(current, i.getB());
            guiItems.add(i);

            current++;
        }
    }

    public void setCloseAction(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public boolean hasActionAtSlot(int slot) {
        return actions.containsKey(slot);
    }

    public ClickAction getClickAction(int slot) {
        if (!actions.containsKey(slot)) return null;

        for (Map.Entry<Integer, ClickAction> clickActionEntry : actions.entrySet()) {
            if (clickActionEntry.getKey() == slot) {
                return clickActionEntry.getValue();
            }
        }

        return null;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public static void closeAll() {
        getGuis().stream().forEach(gui -> {
            gui.getInventory().getViewers().stream().forEach(humanEntity -> {
                if (humanEntity instanceof Player) {
                    Player player = (Player)humanEntity;
                    if (player.getOpenInventory() != null)
                        player.getOpenInventory().close();
                }
            });
        });
    }

}
