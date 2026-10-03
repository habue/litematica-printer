package me.aleksilassila.litematica.printer.v26_1_2.actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.world.entity.player.Input;

public class ReleaseShiftAction extends Action {
    private static final Minecraft mc = Minecraft.getInstance();
    @Override
    public boolean send(Minecraft client, LocalPlayer player) {
        Input input = player.input.keyPresses;
        player.input.keyPresses = new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), mc.options.keyShift.isDown(), input.sprint());

        return true;
    }
}
