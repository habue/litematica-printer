package me.aleksilassila.litematica.printer.v26_1_2.guides;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.actions.Action;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkipGuide extends Guide {
    public SkipGuide(SchematicBlockState state) {
        super(state);
    }

    @Override
    public boolean skipOtherGuides() {
        return true;
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        return false;
    }

    @Override
    public @NotNull List<Action> execute(LocalPlayer player) {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(ItemStack.EMPTY);
    }
}
