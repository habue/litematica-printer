package me.aleksilassila.litematica.printer.v26_1_2.actions;

import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.Printer;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

abstract public class InteractAction extends Action {
    public final PrinterPlacementContext context;

    public InteractAction(PrinterPlacementContext context) {
        this.context = context;
    }

    protected abstract InteractionResult interact(Minecraft client, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);

    @Override
    public boolean send(Minecraft client, LocalPlayer player) {
        interact(client, player, InteractionHand.MAIN_HAND, context.hitResult);

        if (LitematicaMixinMod.DEBUG)
            System.out.println("InteractAction.send: Blockpos: " + context.getBlockPos() + " Side: " + context.getDirection() + " HitPos: " + context.getHitPos());
        BlockPos pos = context.hitResult.isInside() ? context.hitResult.getBlockPos() : context.hitResult.getBlockPos().relative(context.hitResult.getDirection());
        Printer.addTimeout(pos);
        return true;
    }

    @Override
    public String toString() {
        return "InteractAction{" +
                "context=" + context +
                '}';
    }
}
