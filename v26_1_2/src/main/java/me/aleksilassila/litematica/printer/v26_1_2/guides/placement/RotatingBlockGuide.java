package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RotatingBlockGuide extends GeneralPlacementGuide {
    public RotatingBlockGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        Block block = state.targetState.getBlock();
        if (block instanceof WallSkullBlock || block instanceof WallSignBlock || block instanceof WallBannerBlock) {
            Optional<Direction> side = getProperty(state.targetState, BlockStateProperties.HORIZONTAL_FACING).map(Direction::getOpposite);
            return side.map(Collections::singletonList).orElseGet(Collections::emptyList);
        }

        return Collections.singletonList(Direction.DOWN);
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!super.canExecute(player)) return false;

        if (PrinterConfig.PRINTER_IGNORE_ROTATION.getBooleanValue()) return true;

        int rotation = getProperty(state.targetState, BlockStateProperties.ROTATION_16).orElse(0);
        if (targetState.getBlock() instanceof BannerBlock || targetState.getBlock() instanceof SignBlock) {
            rotation = (rotation + 8) % 16;
        }
        int distTo0 = rotation > 8 ? 16 - rotation : rotation;
        float yaw = Math.round(distTo0 / 8f * 180f * (rotation > 8 ? -1 : 1));

        Direction targetDirection;
        float g = yaw * 0.017453292F;
        float j = Mth.sin(g);
        float k = Mth.cos(g);
        boolean bl = j > 0.0F;
        boolean bl3 = k > 0.0F;
        float l = bl ? j : -j;
        float n = bl3 ? k : -k;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        if (l > n) {
            targetDirection = direction;
        } else {
            targetDirection = direction3;
        }

        return player.getDirection() == targetDirection;
    }
}
