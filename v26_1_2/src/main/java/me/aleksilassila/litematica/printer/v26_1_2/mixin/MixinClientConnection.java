package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import io.netty.channel.ChannelHandlerContext;
import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.Printer;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinClientConnection {
    @Unique
    private static final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
        if (!LitematicaMixinMod.PRINT_MODE.getBooleanValue() && !LitematicaMixinMod.PRINT.getKeybind().isPressed()) {
            return;
        }
        if (Printer.inactivityCounter > 20) {
            return;
        }
        if (PrinterConfig.PRINTER_AIRPLACE.getBooleanValue() && PrinterConfig.PRINTER_AIRPLACE_OFFHAND_SLOT_SUPPRESS.getBooleanValue()) {
            if (packet instanceof ClientboundContainerSetSlotPacket packet1) {
                if (packet1.getContainerId() == -2 && packet1.getSlot() == Inventory.SLOT_OFFHAND) {
                    callback.cancel();
                } else if (packet1.getContainerId() == 0 && InventoryMenu.isHotbarSlot(packet1.getSlot())) {
                    if (packet1.getSlot() == InventoryMenu.SHIELD_SLOT) {
                        callback.cancel();
                    }
                }
            }
        }
        // This prevents the server from sending the client the packets that would normally be sent to the client
        // Just wanted to keep this comment above because copilot wrote it. What does that even mean?
        // This prevents 2b from fucking up your inventory with useless slot update packets. Turns out 2b sends slot update
        // packet AND an entire inventory update packet just because. The client does not like that so you get a shit ton of ghost items.
        if (PrinterConfig.PRINTER_SUPER_CHINESE_GHOST_ITEM_FIX.getBooleanValue()) {
            if (packet instanceof ClientboundContainerSetSlotPacket packet1) {
                // Looks like faulty packet is always on syncId = 0
                if (packet1.getContainerId() == 0 && mc.player != null) {
                    if (packet1.getSlot() >= InventoryMenu.USE_ROW_SLOT_START || packet1.getSlot() < InventoryMenu.USE_ROW_SLOT_END) {
                        // Only cancel updates to block items. Some blocks might not be in the USABLE_SLOTS list but still get used for placing
                        if (mc.player.inventoryMenu.getSlot(packet1.getSlot()).getItem().getItem() instanceof BlockItem) {
                            callback.cancel();
                        }
                    }
                }
            }
        }
    }
}
