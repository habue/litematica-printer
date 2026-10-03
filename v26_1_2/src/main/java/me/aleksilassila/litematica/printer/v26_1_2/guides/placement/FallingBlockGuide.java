package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;

public class FallingBlockGuide extends GeneralPlacementGuide {

    public FallingBlockGuide(SchematicBlockState state) {
        super(state);
    }

    boolean blockPlacement() {
        if (targetState.getBlock() instanceof FallingBlock) {
            BlockState below = state.world.getBlockState(state.blockPos.relative(Direction.DOWN));
            return FallingBlock.isFree(below);
        }

        return false;
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (blockPlacement()) return false;

        return super.canExecute(player);
    }

    @Override
    public boolean skipOtherGuides() {
        if (blockPlacement()) return true;

        return super.skipOtherGuides();
    }
}
