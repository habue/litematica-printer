package me.aleksilassila.litematica.printer.v26_1_2.guides;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.actions.Action;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.guides.placement.PropertySpecificGuesserGuide;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.BlockHelperImpl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

abstract public class Guide extends BlockHelperImpl {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public Guide(SchematicBlockState state) {
        this.state = state;

        this.currentState = state.currentState;
        this.targetState = state.targetState;
    }

    protected boolean playerHasRightItem(LocalPlayer player) {
        return getRequiredItemStackSlot(player) != -1;
    }

    public int getSlotWithItem(LocalPlayer player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getNonEquipmentItems().size(); ++i) {
            ItemStack inventoryStack = inventory.getNonEquipmentItems().get(i);
            if (itemStack.isEmpty() && inventoryStack.is(itemStack.getItem())) return i;
            if (!inventoryStack.isEmpty() && ItemStack.isSameItem(inventoryStack, itemStack)) {
                return i;
            }
        }

        return -1;
    }

    protected int getRequiredItemStackSlot(LocalPlayer player) {
        if (player.getAbilities().instabuild) {
            return player.getInventory().getSelectedSlot();
        }

        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);
        if (requiredItem.isEmpty()) return -1;

        return getSlotWithItem(player, requiredItem);
    }

    public boolean canExecute(LocalPlayer player) {
        if (!playerHasRightItem(player)) return false;

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        return !statesEqual(targetState, currentState);
    }

    abstract public @NotNull List<Action> execute(LocalPlayer player);

    abstract protected @NotNull List<ItemStack> getRequiredItems();

    /**
     * Returns the first required item that player has access to,
     * or empty if the items are inaccessible.
     */
    protected List<ItemStack> getRequiredItem(LocalPlayer player) {
        List<ItemStack> requiredItems = getRequiredItems();

        for (ItemStack requiredItem : requiredItems) {
            if (player.getAbilities().instabuild) return List.of(requiredItem);

            int slot = getSlotWithItem(player, requiredItem);
            if (slot > -1)
                return List.of(requiredItem);
        }

        return List.of();
    }

    protected boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2, Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) return false;

        loop:
        for (Property<?> property : state1.getProperties()) {
            if (!PrinterConfig.WATERLOGGING.getBooleanValue() && property == BlockStateProperties.WATERLOGGED && !(state1.getBlock() instanceof CoralBlock)) continue;

            for (Property<?> ignoredProperty : propertiesToIgnore) {
                if (property == ignoredProperty) continue loop;
            }

            try {
                if (state1.getValue(property) != state2.getValue(property)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    protected static <T extends Comparable<T>> Optional<T> getProperty(BlockState blockState, Property<T> property) {
        if (blockState.hasProperty(property)) {
            return Optional.of(blockState.getValue(property));
        }
        return Optional.empty();
    }

    /**
     * Returns true if the two states are equal, ignoring properties that are not relevant
     */
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        // Always ignore redstone-volatile properties that can differ transiently in-world
        // to avoid blocking placement under redstone power.
        Property<?>[] redstoneVolatile = new Property<?>[] {
                BlockStateProperties.POWERED, // many blocks (buttons, rails, etc.)
                BlockStateProperties.TRIGGERED, // dispensers/dropper
                BlockStateProperties.ENABLED // hoppers
        };

        if (PrinterConfig.PRINTER_IGNORE_ROTATION.getBooleanValue()) {
            // Merge rotation ignore with redstone-volatile ignores
            Property<?>[] merged = new Property<?>[PropertySpecificGuesserGuide.rotationProperties.length + redstoneVolatile.length];
            System.arraycopy(PropertySpecificGuesserGuide.rotationProperties, 0, merged, 0, PropertySpecificGuesserGuide.rotationProperties.length);
            System.arraycopy(redstoneVolatile, 0, merged, PropertySpecificGuesserGuide.rotationProperties.length, redstoneVolatile.length);
            return statesEqualIgnoreProperties(state1, state2, merged);
        } else {
            return statesEqualIgnoreProperties(state1, state2, redstoneVolatile);
        }
    }

    public boolean skipOtherGuides() {
        return false;
    }
}
