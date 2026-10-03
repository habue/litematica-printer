package me.aleksilassila.litematica.printer.v26_1_2.actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public abstract class Action {
    abstract public boolean send(Minecraft client, LocalPlayer player);
}
