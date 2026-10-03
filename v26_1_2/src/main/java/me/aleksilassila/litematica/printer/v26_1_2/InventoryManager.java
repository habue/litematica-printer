package me.aleksilassila.litematica.printer.v26_1_2;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.config.options.ConfigString;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class InventoryManager {
    private static final List<Integer> USABLE_SLOTS = new ArrayList<>();
    int delay = 0;
    /**
     * The queue of items to pull from the inventory
     */
    private final ArrayList<SlotInfo> hotbarSlots = new ArrayList<>(9);

    private final Minecraft mc = Minecraft.getInstance();
    private final Deque<Integer> rollingSlots = new ArrayDeque<>();
    private final Deque<Integer> lastUsedSlots = new ArrayDeque<>();
    private static InventoryManager instance;

    private InventoryManager() {
        // Probably add a way to configure this
        for (int i = 0; i < 9; i++) {
            rollingSlots.add(i);
            lastUsedSlots.add(i);
            hotbarSlots.add(new SlotInfo());
        }
    }

    public static void setHotbarSlots(ConfigString config) {
        USABLE_SLOTS.clear();
        String configStr = config.getStringValue();
        String[] parts = configStr.split(",");

        for (String str : parts) {
            try {
                int slotNum = Integer.parseInt(str) - 1;

                if (Inventory.isHotbarSlot(slotNum) &&
                        !USABLE_SLOTS.contains(slotNum)) {
                    USABLE_SLOTS.add(slotNum);
                }
            } catch (NumberFormatException ignore) {
            }
        }
    }

    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    public void reset() {
        hotbarSlots.forEach((info) -> info.ticksLocked = 0);
    }

    public boolean tick() {
        if (mc.player == null || mc.gameMode == null) {
            return false;
        }

        delay = Math.max(0, delay - 1);

        return false;
    }

    /**
     * Swaps the item from the inventory to the hotbar
     *
     * @param item The item to pull from the inventory
     * @return True if the item could the swapped into the hotbar
     */
    private boolean swapToHotbar(LocalPlayer player, Item item) {
        if (getHotbarSlotWithItem(player, new ItemStack(item)) != -1) { // Already in the hotbar dummy
            return true;
        }
        int slot = getBestInventorySlotWithItem(player, new ItemStack(item));
        if (slot == -1) {
            return false;
        }
        int nextSlot = nextHotbarSlot();
        if (nextSlot == -1) {
            return false;
        }
        hotbarSlots.get(nextSlot).addTicksLocked(10);
        hotbarSlots.get(nextSlot).waitingForItem = item;
        player.getInventory().setSelectedSlot(nextSlot);
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) {
            System.out.println("Swapping item from inventory: " + slot + " into hotbar -> " + nextSlot);
        }
        swapAway(slot, nextSlot, player.inventoryMenu);
        delay += PrinterConfig.INVENTORY_DELAY.getIntegerValue();
        return true;
    }

    public void pickSlot(WorldSchematic world, LocalPlayer player, BlockPos pos) {
        if (mc.gameMode == null) {
            return;
        }
        Inventory inv = player.getInventory();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(state, world, pos);
        int slot = inv.findSlotMatchingItem(stack);
        boolean shouldPick = slot > 8;
        if (slot != -1 && !shouldPick) {
            player.getInventory().setSelectedSlot(slot);
        } else if (slot != -1) {
            InventoryUtils.setPickedItemToHand(slot, stack, mc); // https://github.com/sakura-ryoko/litematica-printer/blob/f8e38a2b31708e61f8a5fad0f2989d6834495da4/src/main/java/me/aleksilassila/litematica/printer/actions/PrepareAction.java#L71
        } else if (Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
            slot = findSlotWithBoxWithItem(player.getInventory(), stack, true);
            if (slot > -1) {
                if (slot > 8) {
                    InventoryUtils.setPickedItemToHand(slot, stack, mc);
                } else {
                    inv.setSelectedSlot(slot);
                }
            }
        }
    }

    private void depositCursorStack() {
        mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
//        if (!mc.player.inventoryMenu.getCursorStack().isEmpty()) {
//            mc.player.getInventory().offerOrDrop(mc.player.inventoryMenu.getCursorStack());
//            mc.player.inventoryMenu.setCursorStack(ItemStack.EMPTY);
//        }
    }

    public static int findSlotWithBoxWithItem(Inventory inventory, ItemStack stackReference, boolean lestFirst) {
        int bestCount = lestFirst ? Integer.MAX_VALUE : 0;
        int bestSlot = -1;

        for (int slotNum = 0; slotNum < inventory.getNonEquipmentItems().size(); slotNum += 1) {
            ItemStack itemStack = inventory.getItem(slotNum);
            int count = shulkerBoxItemCount(itemStack, stackReference);
            if (lestFirst && count < bestCount && count > 0) {
                bestCount = count;
                bestSlot = slotNum;
            } else if (!lestFirst && count > bestCount) {
                bestCount = count;
                bestSlot = slotNum;
            }
        }

        return bestSlot;
    }

    public static int shulkerBoxItemCount(ItemStack stack, ItemStack referenceItem) {
        NonNullList<ItemStack> items = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(stack);
        int count = 0;
        if (!items.isEmpty()) {
            for (ItemStack item : items) {
                if (fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(item, referenceItem)) {
                    count += item.getCount();
                }
            }
        }

        return count;
    }

    public boolean select(ItemStack itemStack) {
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) {
            return false;
        }

        // Swap logic start
        if (itemStack != null) {
            Inventory inventory = player.getInventory();

            // This thing is straight from Minecraft#doItemPick()
            if (player.getAbilities().instabuild) {
                depositCursorStack();
                this.addPickBlock(inventory, itemStack);
                mc.gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.getSelectedSlot());
                updateLastUsedSlot(inventory.getSelectedSlot());
                return true;
            } else {
                int hotbarSlot = getHotbarSlotWithItem(player, itemStack);
                if (hotbarSlot == -1) {
                    // Start equipping from inventory
                    if (delay > 0) {
                        return false;
                    }
                    depositCursorStack();
                    if (swapToHotbar(mc.player, itemStack.getItem())) {
                        // If true the item should now be somewhere in the hotbar
                        hotbarSlot = getHotbarSlotWithItem(player, itemStack);
                        if (hotbarSlot == -1) {
                            return false;
                        }
                        if (hotbarSlot != player.getInventory().getSelectedSlot()) {
                            player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
                            player.getInventory().setSelectedSlot(hotbarSlot);
                        }
                        updateLastUsedSlot(hotbarSlot);
                        return false;
                    }
                    return false;
                } else {
                    depositCursorStack();
                    // Switch to hotbar slot
                    if (hotbarSlot != player.getInventory().getSelectedSlot()) {
                        player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
                        player.getInventory().setSelectedSlot(hotbarSlot);
                    }
                    updateLastUsedSlot(hotbarSlot);
                    return true;
                }
            }
        }
        return false;
    }

    // https://github.com/sakura-ryoko/litematica-printer/blob/f8e38a2b31708e61f8a5fad0f2989d6834495da4/src/main/java/me/aleksilassila/litematica/printer/actions/PrepareAction.java#L95
    private void addPickBlock(Inventory inv, ItemStack stack) {
        int slot = inv.findSlotMatchingItem(stack);

        if (slot >= 0 && slot < 9) {
            inv.setSelectedSlot(slot);
        } else {
            if (slot == -1) {
                inv.setSelectedSlot(inv.getSuitableHotbarSlot());

                if (!inv.getNonEquipmentItems().get(inv.getSelectedSlot()).isEmpty()) {
                    int empty = inv.getFreeSlot();

                    if (empty != -1) {
                        inv.setItem(empty, inv.getNonEquipmentItems().get(inv.getSelectedSlot()));
                    }
                }
                inv.setItem(inv.getSelectedSlot(), stack);
            } else {
                inv.pickSlot(slot);
            }
        }
    }

    public boolean swapAway(int inventorySlot, int hotbarSlot, InventoryMenu screenHandler) {
        ClientPacketListener networkHandler = mc.getConnection();
        if (mc.player == null || mc.gameMode == null || networkHandler == null) return false;

        mc.gameMode.handleContainerInput(screenHandler.containerId, inventorySlot, hotbarSlot, ContainerInput.SWAP, mc.player);
        return true;
    }

    /**
     * Returns the next available slot for a new item. Returns the slot to the end of the queue after returning.
     * Range from 0-8
     *
     * @return The next available slot
     */
    private int nextHotbarSlot() {
        final String mode = PrinterConfig.PRINTER_INVENTORY_MANAGEMENT_MODE.getStringValue();

        if (PrinterConfig.InventoryManagementModeEnum.LEAST_USED.is(mode)) {
            if (!lastUsedSlots.isEmpty()) {
                List<Integer> usableSlots = lastUsedSlots.stream().filter(USABLE_SLOTS::contains).toList();
                if (usableSlots.isEmpty()) {
                    return -1;
                }
                int last = usableSlots.getLast();
                lastUsedSlots.remove(last);
                lastUsedSlots.addFirst(last);
                if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) {
                    System.out.println("Next new least used slot: " + last + "Last used slots: " + lastUsedSlots);
                }
                return last;
            }
        } else if (PrinterConfig.InventoryManagementModeEnum.ROLLING.is(mode)) {
            if (!rollingSlots.isEmpty()) {
                List<Integer> usableSlots = rollingSlots.stream().filter(USABLE_SLOTS::contains).toList();
                if (usableSlots.isEmpty()) {
                    return -1;
                }
                int slot = usableSlots.getLast();
                rollingSlots.remove(slot);
                rollingSlots.addLast(slot);
                if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) {
                    System.out.println("Next new rolling slot: " + slot + "Rolling slots: " + rollingSlots);
                }
                return slot;
            }
        }
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) {
            System.out.println("No slots available");
        }
        return -1;
    }

    /**
     * Returns the first slot with the given item. Returns -1 if no slot is found.
     *
     * @param player    The player to check
     * @param itemStack The item to check for
     * @return The first slot with the given item
     */
    private static int getBestInventorySlotWithItem(LocalPlayer player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        if (itemStack.isEmpty()) return -1;

        int lowestCount = 0;
        int lowestSlot = -1;
        for (int i = 9; i < inventory.getNonEquipmentItems().size(); ++i) {
            ItemStack inventoryStack = inventory.getNonEquipmentItems().get(i);
            if (!inventoryStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, inventoryStack)) {
                if (inventoryStack.getCount() < lowestCount || lowestSlot == -1) {
                    lowestCount = inventoryStack.getCount();
                    lowestSlot = i;
                }
            }
        }

        return lowestSlot;
    }

    /**
     * Return a number between 0-8 representing the hotbar slot with the given item.
     */
    public int getHotbarSlotWithItem(LocalPlayer player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        if (itemStack.isEmpty()) return -1;

        for (int i = 0; i < 9; ++i) {
            if (!inventory.getNonEquipmentItems().get(i).isEmpty() && ItemStack.isSameItem(inventory.getNonEquipmentItems().get(i), itemStack)) {
//                if (hotbarSlots.get(i).ticksLocked == 0) {
                return i;
//                }
            }
        }

        return -1;
    }

    private void updateLastUsedSlot(int slot) {
        lastUsedSlots.remove(slot);
        lastUsedSlots.addFirst(slot);
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) {
            String list = lastUsedSlots.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("");
            System.out.println("Updating last used slot: " + slot + ". Now: " + list);
        }
    }

    static class SlotInfo {
        private int ticksLocked = 0;
        @Nullable
        public Item waitingForItem = null;

        public void addTicksLocked(int ticks) {
            ticksLocked += ticks;
        }

        public void decrementTicksLocked() {
            ticksLocked--;
        }
    }
}
