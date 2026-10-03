package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {
    @Inject(method = "aiStep", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (PrinterConfig.PREVENT_DOUBLE_TAP_SPRINTING.getBooleanValue()) {
            ((MixinAccessorClientPlayerEntity) (Object) this).setTicksLeftToDoubleTapSprint(0);
        }
    }
}
