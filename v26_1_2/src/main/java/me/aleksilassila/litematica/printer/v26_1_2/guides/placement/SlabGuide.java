package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.List;

public class SlabGuide extends GeneralPlacementGuide {
    public SlabGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        return Arrays.stream(Direction.values())
                .filter(d -> d != (getRequiredHalf(state).getOpposite()) &&
                        getProperty(state.offset(d).currentState, SlabBlock.TYPE).orElse(SlabType.DOUBLE) == SlabType.DOUBLE)
                .toList();
    }

    private Direction getRequiredHalf(SchematicBlockState state) {
        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        if (!currentState.hasProperty(SlabBlock.TYPE)) {
            return targetState.getValue(SlabBlock.TYPE) == SlabType.TOP ? Direction.UP : Direction.DOWN;
        } else if (currentState.getValue(SlabBlock.TYPE) != targetState.getValue(SlabBlock.TYPE)) {
            return currentState.getValue(SlabBlock.TYPE) == SlabType.TOP ? Direction.DOWN : Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }
}
