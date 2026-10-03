package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FacingBlockGuide extends SlabGuide {
    public FacingBlockGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        Block block = state.targetState.getBlock();
        if (block instanceof WallSkullBlock || block instanceof WallSignBlock || block instanceof WallBannerBlock) {
            Optional<Direction> side = getProperty(state.targetState, BlockStateProperties.HORIZONTAL_FACING).map(Direction::getOpposite);
            return side.map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
        if (block instanceof StairBlock) {
            Direction half = getRequiredHalf(state).getOpposite();
            return Arrays.stream(Direction.values()).filter(d -> d != half).toList();
        }

        return Arrays.stream(Direction.values()).toList();
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    private Direction getRequiredHalf(SchematicBlockState state) {
        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        if (!currentState.hasProperty(StairBlock.HALF)) {
            return targetState.getValue(StairBlock.HALF) == Half.TOP ? Direction.UP : Direction.DOWN;
        } else if (currentState.getValue(StairBlock.HALF) != targetState.getValue(StairBlock.HALF)) {
            return currentState.getValue(StairBlock.HALF) == Half.TOP ? Direction.DOWN : Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }
}
