package me.aleksilassila.litematica.printer.v26_1_2.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface MixinAccessorClientPlayerEntity {
    @Accessor("sprintTriggerTime")
    void setTicksLeftToDoubleTapSprint(int ticksLeftToDoubleTapSprint);

    @Accessor("crouching")
    boolean getLastSneaking();

    @Accessor("crouching")
    void setLastSneaking(boolean lastSneaking);

    @Accessor("xRotLast")
    float getLastPitch();
    @Accessor("xRotLast")
    void setLastPitch(float lastPitch);

    @Accessor("yRotLast")
    float getLastYaw();
    @Accessor("yRotLast")
    void setLastYaw(float lastYaw);
}
