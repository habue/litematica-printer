package me.aleksilassila.litematica.printer.v26_1_2.actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;
import java.util.List;

public class ActionChain extends Action {
    List<Action> actionsCurrentTick = new ArrayList<>();
    List<Action> actionsNextTick = new ArrayList<>();

    @Override
    public boolean send(Minecraft client, LocalPlayer player) {
        throw new RuntimeException("ActionChain should not be sent. Manually send each preTick and postTick action.");
    }

    public void addImmediateAction(Action action) {
        actionsCurrentTick.add(action);
    }
    public void addNextTickAction(Action action) {
        actionsNextTick.add(action);
    }

    public void clear() {
        actionsCurrentTick.clear();
    }

    public List<Action> getActionsCurrentTick() {
        return actionsCurrentTick;
    }
    public List<Action> getActionsNextTick() {
        return actionsNextTick;
    }
}
