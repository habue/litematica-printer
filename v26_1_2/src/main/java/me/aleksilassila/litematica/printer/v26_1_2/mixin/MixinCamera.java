package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import me.aleksilassila.litematica.printer.v26_1_2.FreeLook;
import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = At.Shift.AFTER))
    private void onUpdateAfterAlignWithEntity(CallbackInfo ci) {
        FreeLook freeLook = LitematicaMixinMod.freeLook;

        if (freeLook.isEnabled()) {
            setRotation(freeLook.getCameraYaw(), freeLook.getCameraPitch());
        }
    }
}
