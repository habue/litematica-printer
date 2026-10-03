package me.aleksilassila.litematica.printer.v26_1_2.actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.world.entity.player.Input;

public class PresShift extends Action {
    @Override
    public boolean send(Minecraft client, LocalPlayer player) {
        Input input = player.input.keyPresses;
        player.input.keyPresses = new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), true, input.sprint());
        // 26.1.2 uses input packets for the shift press/release state; sneak travels in the input packet alone.

        return true;
    }
}
