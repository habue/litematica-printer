package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import me.aleksilassila.litematica.printer.v26_1_2.FreeLook;
import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHandler.class)
public class MixinMouse {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Shadow @Final private Minecraft minecraft;
    @Inject(method = "turnPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getTutorial()Lnet/minecraft/client/tutorial/Tutorial;",
                    shift = At.Shift.BEFORE
            ), cancellable = true
    )
    private void updateMouseChangeLookDirection(double timeDelta, CallbackInfo ci) {
        FreeLook freeLook = LitematicaMixinMod.freeLook;
        if (freeLook.isEnabled()) {
            double f = this.minecraft.options.sensitivity().get() * 0.6000000238418579 + 0.20000000298023224;
            double g = f * f * f;
            double h = g * 8.0;
            double k = this.accumulatedDX * h;
            double l = this.accumulatedDY * h;
            int m = 1;
            if (this.minecraft.options.invertMouseY().get()) {
                m = -1;
            }
            float yaw = (float) (freeLook.getCameraYaw() + k * 0.15F);
            freeLook.setCameraYaw(yaw);
            float pitch = Mth.clamp((float) (freeLook.getCameraPitch() + (l * (double) m) * 0.15F), -90.0F, 90.0F);
            freeLook.setCameraPitch(pitch);
            if (Math.abs(pitch) > 90.0F) {
                yaw = pitch > 0.0F ? 90.0F : -90.0F;
                freeLook.setCameraYaw(yaw);
            }
            accumulatedDX = 0.0;
            accumulatedDY = 0.0;
            if (freeLook.shouldRotate()) {
                if (this.minecraft.player != null) {
                    this.minecraft.player.turn(k, l * (double)m);
                    this.minecraft.player.setYRot(freeLook.getCameraYaw());
                    this.minecraft.player.setXRot(freeLook.getCameraPitch());
                }
            }
            ci.cancel();
        }
    }
}
