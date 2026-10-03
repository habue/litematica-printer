package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TorchGuide extends PropertySpecificGuesserGuide {
    public TorchGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        // Prefer the wall-mounted FACING for wall torches; fallback to horizontal facing
        Optional<Direction> facing = getProperty(targetState, FaceAttachedHorizontalDirectionalBlock.FACING);
        if (facing.isEmpty()) facing = getProperty(targetState, HorizontalDirectionalBlock.FACING);

        return facing
                .map(direction -> Collections.singletonList(direction.getOpposite()))
                .orElseGet(() -> Collections.singletonList(Direction.DOWN));
    }

    @Override
    protected Optional<Block> getRequiredItemAsBlock(LocalPlayer player) {
        return Optional.of(state.targetState.getBlock());
    }
}
