package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * This class apparently fixes an issue with Quilt.
 */
@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    @Accessor("STRIPPABLES")
    static Map<Block, Block> getStrippedBlocks() {
        throw new AssertionError("Untransformed @Accessor");
    }

}
