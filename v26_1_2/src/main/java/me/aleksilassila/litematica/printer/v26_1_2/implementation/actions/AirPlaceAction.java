package me.aleksilassila.litematica.printer.v26_1_2.implementation.actions;

import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.actions.InteractAction;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class AirPlaceAction extends InteractAction {
    private final Minecraft mc = Minecraft.getInstance();
    public AirPlaceAction(PrinterPlacementContext context) {
        super(context);
    }

    @Override
    protected InteractionResult interact(Minecraft client, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.isInside() ? hitResult.getBlockPos() : hitResult.getBlockPos().relative(hitResult.getDirection());

        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        airPlace(pos);
        return InteractionResult.PASS;
    }

    private void airPlace(BlockPos pos) {
        ClientPacketListener connection = mc.getConnection();
        MultiPlayerGameMode interactionManager = mc.gameMode;
        if (mc.player == null || connection == null || interactionManager == null) return;

        if (mc.hasSingleplayerServer() && mc.player.getAbilities().instabuild) {
            BlockHitResult blockHitResult = new BlockHitResult(this.context.hitResult.getLocation(), this.context.hitResult.getDirection(), pos, true);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return;
        }

        // The raw SWAP_ITEM_WITH_OFFHAND packet swaps hands server-side only, so the client
        // inventory must be swapped in lockstep or the useItemOn prediction below runs against
        // a stale offhand stack and the two sides drift apart (the old offhand-eviction +
        // slot-update-suppression approach; it dropped offhand items when the inventory was full).
        swapHandStacksLocally(mc.player);
        connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));

        InteractionHand hand = InteractionHand.OFF_HAND;
        BlockHitResult blockHitResult = new BlockHitResult(this.context.hitResult.getLocation(), this.context.hitResult.getDirection(), pos, true);
        interactionManager.useItemOn(mc.player, hand, blockHitResult);
        mc.player.swing(InteractionHand.MAIN_HAND);
        connection.send(new ServerboundSwingPacket(hand));
        swapHandStacksLocally(mc.player);
        connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }

    /**
     * Mirrors what the server does when it receives SWAP_ITEM_WITH_OFFHAND
     * (ServerGamePacketListenerImpl swaps the two hand stacks via setItemInHand).
     */
    private void swapHandStacksLocally(LocalPlayer player) {
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, offStack);
        player.setItemInHand(InteractionHand.OFF_HAND, mainStack);
    }
}
