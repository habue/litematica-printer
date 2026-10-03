package me.aleksilassila.litematica.printer.v26_1_2.implementation.actions;


import me.aleksilassila.litematica.printer.v26_1_2.actions.InteractAction;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

/*
 * @author IceTank
 * @since 08.03.2026
 */
public class UseItemActionImpl extends InteractAction {
    public UseItemActionImpl(PrinterPlacementContext context) {
        super(context);
    }
    private final Minecraft mc = Minecraft.getInstance();
    @Override
    protected InteractionResult interact(Minecraft client, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = client.gameMode.useItem(player, hand);
        if (!result.consumesAction()) {
            if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) System.out.println("Failed to interact with block got " + result);
        }
        mc.player.swing(InteractionHand.MAIN_HAND);
        return result;
    }
}
