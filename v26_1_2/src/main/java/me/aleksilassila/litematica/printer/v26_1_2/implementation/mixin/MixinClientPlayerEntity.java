package me.aleksilassila.litematica.printer.v26_1_2.implementation.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.Printer;
import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.UpdateChecker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity extends AbstractClientPlayer {

    private static boolean didCheckForUpdates = false;

    @Shadow
    protected Minecraft minecraft;
    @Shadow
    public ClientPacketListener connection;

    public MixinClientPlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (!didCheckForUpdates) {
            didCheckForUpdates = true;

            checkForUpdates();
        }

        if (LitematicaMixinMod.freeLook.getPrevPerspective() == null) {
            LitematicaMixinMod.freeLook.setPrevPerspective(minecraft.options.getCameraType());
        }
        if (LitematicaMixinMod.printer != null) {
//            LitematicaMixinMod.printer.actionHandler.onPostTick();
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tickHead(CallbackInfo ci) {
        LocalPlayer clientPlayer = (LocalPlayer) (Object) this;
        if (LitematicaMixinMod.printer == null || LitematicaMixinMod.printer.player != clientPlayer) {
            System.out.println("Initializing printer, player: " + clientPlayer + ", client: " + minecraft);
            LitematicaMixinMod.printer = new Printer(minecraft, clientPlayer);
        }
        LitematicaMixinMod.printer.actionHandler.processPreviousTickActions();
        LitematicaMixinMod.printer.onGameTick();
        LitematicaMixinMod.printer.actionHandler.processCurrentTickActions();
        Printer.inventoryManager.tick();
        LitematicaMixinMod.freeLook.onGameTick();
        LitematicaMixinMod.movementHandler.onGameTick();
    }

    public void checkForUpdates() {
        new Thread(() -> {
            String version = UpdateChecker.version;
            String newVersion = UpdateChecker.getPrinterVersion();

            if (!version.equals(newVersion)) {
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.literal("New version of Litematica Printer available in https://github.com/aleksilassila/litematica-printer/releases"));
                }
            }
        }).start();
    }

    @Inject(method = "openTextEdit", at = @At("HEAD"), cancellable = true)
    public void openEditSignScreen(SignBlockEntity sign, boolean front, CallbackInfo ci) {
        getTargetSignEntity(sign).ifPresent(signBlockEntity -> {
            ServerboundSignUpdatePacket packet = new ServerboundSignUpdatePacket(sign.getBlockPos(),
                    front,
                    signBlockEntity.getText(front).getMessage(0, false).getString(),
                    signBlockEntity.getText(front).getMessage(1, false).getString(),
                    signBlockEntity.getText(front).getMessage(2, false).getString(),
                    signBlockEntity.getText(front).getMessage(3, false).getString());
            this.connection.send(packet);
            ci.cancel();
        });
    }

    private Optional<SignBlockEntity> getTargetSignEntity(SignBlockEntity sign) {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        SchematicBlockState state = new SchematicBlockState(sign.getLevel(), worldSchematic, sign.getBlockPos());

        BlockEntity targetBlockEntity = worldSchematic.getBlockEntity(state.blockPos);

        if (targetBlockEntity instanceof SignBlockEntity targetSignEntity) {
            return Optional.of(targetSignEntity);
        }

        return Optional.empty();
    }
}
