package me.aleksilassila.litematica.printer.v26_1_2.guides.interaction;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CycleStateGuide extends InteractionGuide {
    private static final Property<?>[] propertiesToIgnore = new Property[]{
            BlockStateProperties.POWERED,
            BlockStateProperties.LIT,
            BlockStateProperties.ATTACH_FACE,
            BlockStateProperties.FACING,
            BlockStateProperties.LOCKED,
            BlockStateProperties.HALF,
            BlockStateProperties.DOOR_HINGE,
            BlockStateProperties.IN_WALL,
            RepeaterBlock.FACING
    };

    public CycleStateGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!playerHasRightItem(player)) return false;

        if (currentState.getBlock() == Blocks.IRON_TRAPDOOR) {
            return false; // Iron trapdoors cannot be toggled by interaction
        }

        if (currentState.getBlock() != targetState.getBlock()) {
            return false; // Different blocks cannot be toggled
        }

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        if (currentState.getBlock() == Blocks.LEVER) {
            if (currentState.getValue(LeverBlock.POWERED) == targetState.getValue(LeverBlock.POWERED)) {
                return false; // Lever blocks must be toggled if POWERED property is incorrect, regardless of other properties
            }
            return true;
        }
        return !statesEqualIgnoreProperties(targetState, currentState, propertiesToIgnore);
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
