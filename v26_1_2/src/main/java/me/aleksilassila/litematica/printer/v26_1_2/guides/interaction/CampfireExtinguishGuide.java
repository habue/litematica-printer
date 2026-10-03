package me.aleksilassila.litematica.printer.v26_1_2.guides.interaction;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CampfireExtinguishGuide extends InteractionGuide {
    boolean shouldBeLit;
    boolean isLit;

    public CampfireExtinguishGuide(SchematicBlockState state) {
        super(state);

        shouldBeLit = getProperty(targetState, CampfireBlock.LIT).orElse(false);
        isLit = getProperty(currentState, CampfireBlock.LIT).orElse(false);
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!super.canExecute(player)) return false;

        return (currentState.getBlock() instanceof CampfireBlock) && !shouldBeLit && isLit;
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Arrays.stream(SHOVEL_ITEMS).map(ItemStack::new).toList();
    }
}
