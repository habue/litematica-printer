package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.guides.interaction.LogStrippingGuide;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogGuide extends GeneralPlacementGuide {
    public LogGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    protected List<Direction> getPossibleSides() {
        if (targetState.hasProperty(RotatedPillarBlock.AXIS)) {
            Direction.Axis axis = targetState.getValue(RotatedPillarBlock.AXIS);
            return Arrays.stream(Direction.values()).filter(d -> d.getAxis() == axis).toList();
        }

        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        for (Block log : LogStrippingGuide.STRIPPED_BLOCKS.keySet()) {
            if (targetState.getBlock() == LogStrippingGuide.STRIPPED_BLOCKS.get(log)) {
                return Collections.singletonList(new ItemStack(log));
            }
        }

        return super.getRequiredItems();
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!LitematicaMixinMod.STRIP_LOGS.getBooleanValue()) return false;

        if (LogStrippingGuide.STRIPPED_BLOCKS.containsValue(targetState.getBlock())) {
            return super.canExecute(player);
        }

        return false;
    }
}
