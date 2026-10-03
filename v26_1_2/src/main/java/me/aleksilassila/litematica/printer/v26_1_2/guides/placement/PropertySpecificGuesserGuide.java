package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import java.util.stream.Stream;

public class PropertySpecificGuesserGuide extends GeneralPlacementGuide {
    protected static Property<?>[] ignoredProperties = new Property[]{
            RepeaterBlock.DELAY,
            ComparatorBlock.MODE,
            ComposterBlock.LEVEL,
            RedStoneWireBlock.POWER,
            RedStoneWireBlock.EAST,
            RedStoneWireBlock.NORTH,
            RedStoneWireBlock.SOUTH,
            RedStoneWireBlock.WEST,
            BlockStateProperties.POWERED,
            BlockStateProperties.TRIGGERED,
            BlockStateProperties.OPEN,
            PointedDripstoneBlock.THICKNESS,
            ScaffoldingBlock.DISTANCE,
            ScaffoldingBlock.BOTTOM,
            CactusBlock.AGE,
            BambooStalkBlock.AGE,
            BambooStalkBlock.LEAVES,
            BambooStalkBlock.STAGE,
            SaplingBlock.STAGE,
            BlockStateProperties.EAST,
            BlockStateProperties.NORTH,
            BlockStateProperties.SOUTH,
            BlockStateProperties.WEST,
            SnowLayerBlock.LAYERS,
            SeaPickleBlock.PICKLES,
            CandleBlock.CANDLES,
            EndPortalFrameBlock.HAS_EYE,
            BlockStateProperties.LIT,
            LeavesBlock.DISTANCE,
            LeavesBlock.PERSISTENT,
            BlockStateProperties.ATTACHED,
            BlockStateProperties.NOTE,
            BlockStateProperties.NOTEBLOCK_INSTRUMENT,
            BlockStateProperties.EXTENDED,
            BlockStateProperties.WEST_WALL,
            BlockStateProperties.EAST_WALL,
            BlockStateProperties.NORTH_WALL,
            BlockStateProperties.SOUTH_WALL,
            BlockStateProperties.ENABLED
    };

    public static Property<?>[] rotationProperties = new Property[]{
            BlockStateProperties.ROTATION_16,
            BlockStateProperties.HORIZONTAL_FACING,
            BlockStateProperties.AXIS,
            BlockStateProperties.HORIZONTAL_AXIS
    };

    public PropertySpecificGuesserGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected boolean statesEqual(BlockState resultState, BlockState targetState) {
        if (PrinterConfig.PRINTER_IGNORE_ROTATION.getBooleanValue()) {
            // Combine rotation properties with ignored properties
            return statesEqualIgnoreProperties(resultState, targetState, Stream.concat(
                    Stream.of(rotationProperties),
                    Stream.of(ignoredProperties)
            ).toArray(Property[]::new));
        }
        return statesEqualIgnoreProperties(resultState, targetState, ignoredProperties);
    }
}
