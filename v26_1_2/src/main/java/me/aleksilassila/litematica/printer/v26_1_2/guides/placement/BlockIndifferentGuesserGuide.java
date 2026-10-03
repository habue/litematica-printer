package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A GuesserGuide that ignores certain block state properties for specific blocks.
 */
public class BlockIndifferentGuesserGuide extends GeneralPlacementGuide {
    public BlockIndifferentGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        Block targetBlock = targetState.getBlock();
        Block resultBlock = resultState.getBlock();

        if (targetBlock instanceof BambooStalkBlock) {
            return resultBlock instanceof BambooStalkBlock || resultBlock instanceof BambooSaplingBlock;
        }

        if (targetBlock instanceof BigDripleafStemBlock) {
            if (resultBlock instanceof BigDripleafBlock || resultBlock instanceof BigDripleafStemBlock) {
                return resultState.getValue(HorizontalDirectionalBlock.FACING) == targetState.getValue(HorizontalDirectionalBlock.FACING);
            }
        }

        if (targetBlock instanceof TwistingVinesPlantBlock) {
            if (resultBlock instanceof TwistingVinesBlock) {
                return true;
            } else if (resultBlock instanceof TwistingVinesPlantBlock) {
                return statesEqualIgnoreProperties(resultState, targetState, TwistingVinesBlock.AGE);
            }
        }

        if (targetBlock instanceof TripWireBlock && resultBlock instanceof TripWireBlock) {
            return statesEqualIgnoreProperties(resultState, targetState,
                    TripWireBlock.ATTACHED, TripWireBlock.DISARMED, TripWireBlock.POWERED, TripWireBlock.NORTH,
                    TripWireBlock.EAST, TripWireBlock.SOUTH, TripWireBlock.WEST);
        }

        if (targetBlock instanceof MushroomBlock) {
            return statesEqualIgnoreProperties(resultState, targetState, BlockStateProperties.DOWN, BlockStateProperties.UP, BlockStateProperties.WEST, BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH);
        }

        if (targetBlock instanceof GlowLichenBlock && PrinterConfig.PRINTER_ALLOW_NONE_EXACT_STATES.getBooleanValue()) {
            return resultState.getBlock() == targetState.getBlock();
        }

        return super.statesEqual(resultState, targetState);
    }
}
