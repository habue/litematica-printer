package me.aleksilassila.litematica.printer.v26_1_2.implementation;

import me.aleksilassila.litematica.printer.v26_1_2.BlockHelper;
import net.minecraft.world.level.block.ButtonBlock;

import java.util.Arrays;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                ButtonBlock.class
        ));
    }
}
