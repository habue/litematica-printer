package me.aleksilassila.litematica.printer.v26_1_2;

import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.mixin.MixinAccessorClientPlayerEntity;
import me.aleksilassila.litematica.printer.v26_1_2.mixin.MixinAccessorKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class MovementHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private boolean disableNextTick = false;

    public void onGameTick() {
        if (mc.player == null) return;

        // Disabled check
        if (!PrinterConfig.FREE_LOOK.getBooleanValue() || !LitematicaMixinMod.PRINT_MODE.getBooleanValue()) {
            if (disableNextTick) {
                disableNextTick = false;
                InputDirections.apply(InputDirections.getCurrentInput());
            }
            return;
        }

        // Disable in inventories
        if (mc.screen != null) {
            if (PrinterConfig.MOVE_WHILE_IN_INVENTORY.getBooleanValue()) {
                if (mc.screen instanceof CraftingScreen || mc.screen instanceof CreativeModeInventoryScreen) {
                    return;
                }
            } else {
                return;
            }
        }

        // Get current inputs
        InputDirections currentInputDirection = InputDirections.getCurrentInput();

        float cameraYaw = mc.gameRenderer.getMainCamera().yRot() % 360;
        if (currentInputDirection != InputDirections.NONE) {
            // lastPlayerInputDirection = currentInputDirection;
            // Calculate resulting control direction and apply them
            float inputYaw = currentInputDirection.getYaw();
            float playerYaw = (mc.player.getYRot() + 360) % 360;

            // Calculate the result yaw
            float playerRelativeYaw = inputYaw + playerYaw;
            float resultPlayerYaw = playerRelativeYaw - cameraYaw;
            InputDirections resultDirection = InputDirections.getDirection(resultPlayerYaw);
            if (resultDirection == null || resultDirection == InputDirections.NONE) {
                // ChatUtil.sendClientMessage("Result direction is null");
                Printer.logger.warn("Result direction is null / None");
                return;
            }

            InputDirections.apply(resultDirection);
        } else /*if (overwriteKeys.get())*/ {
//            Printer.logger.info("No input direction");
            InputDirections.apply(InputDirections.NONE);
        }
    }

    public static void grimRotate(LocalPlayer player, float yaw, float pitch) {
        Vec3 playerPos = player.position();
        mc.getConnection().send(new ServerboundPlayerInputPacket(player.input.keyPresses));
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(playerPos.x, playerPos.y, playerPos.z, yaw, pitch, player.onGround(), player.horizontalCollision));
        ((MixinAccessorClientPlayerEntity) mc.player).setLastYaw(yaw);
        ((MixinAccessorClientPlayerEntity) mc.player).setLastPitch(pitch);
    }

    public void onDisable(LocalPlayer player) {
        disableNextTick = true;
    }

    enum InputDirections {
        FORWARD(0),
        FORWARD_LEFT(45),
        FORWARD_RIGHT(315),
        LEFT(90),
        RIGHT(270),
        BACK(180),
        BACK_LEFT(135),
        BACK_RIGHT(225),
        NONE(-1);
        private final float yaw;

        InputDirections(float i) {
            this.yaw = i;
        }

        static InputDirections getDirection(float yaw) {
            while (yaw < 0) yaw += 360;
            yaw = yaw % 360;
            if (yaw < 22.5) return FORWARD;
            if (yaw < 67.5) return FORWARD_LEFT;
            if (yaw < 112.5) return LEFT;
            if (yaw < 157.5) return BACK_LEFT;
            if (yaw < 202.5) return BACK;
            if (yaw < 247.5) return BACK_RIGHT;
            if (yaw < 292.5) return RIGHT;
            if (yaw < 337.5) return FORWARD_RIGHT;
            return FORWARD;
        }

        /**
         * Returns a unit vector in the direction of the input direction
         *
         * @return a unit vector in the direction of the input direction
         */
        public Vec3 getVec3d() {
            return new Vec3(Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        }

        public float getYaw() {
            return yaw;
        }

        static InputDirections getCurrentInput() {
            if (isKeyPressed(mc.options.keyUp)) {
                if (isKeyPressed(mc.options.keyLeft)) {
                    return FORWARD_LEFT;
                } else if (isKeyPressed(mc.options.keyRight)) {
                    return FORWARD_RIGHT;
                } else {
                    return FORWARD;
                }
            } else if (isKeyPressed(mc.options.keyDown)) {
                if (isKeyPressed(mc.options.keyLeft)) {
                    return BACK_LEFT;
                } else if (isKeyPressed(mc.options.keyRight)) {
                    return BACK_RIGHT;
                } else {
                    return BACK;
                }
            } else if (isKeyPressed(mc.options.keyLeft)) {
                return LEFT;
            } else if (isKeyPressed(mc.options.keyRight)) {
                return RIGHT;
            } else {
                return NONE;
            }
        }

        public boolean isPressed() {
            switch (this) {
                case FORWARD -> isKeyPressed(mc.options.keyUp);
                case FORWARD_LEFT -> {
                    return isKeyPressed(mc.options.keyUp) && isKeyPressed(mc.options.keyLeft);
                }
                case FORWARD_RIGHT -> {
                    return isKeyPressed(mc.options.keyUp) && isKeyPressed(mc.options.keyRight);
                }
                case LEFT -> isKeyPressed(mc.options.keyLeft);
                case RIGHT -> isKeyPressed(mc.options.keyRight);
                case BACK -> isKeyPressed(mc.options.keyDown);
                case BACK_LEFT -> {
                    return isKeyPressed(mc.options.keyDown) && isKeyPressed(mc.options.keyLeft);
                }
                case BACK_RIGHT -> {
                    return isKeyPressed(mc.options.keyDown) && isKeyPressed(mc.options.keyRight);
                }
                default -> {
                    return false;
                }
            }
            return false;
        }

        static void apply(InputDirections direction) {
            mc.options.keyUp.setDown(false);
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);
            mc.options.keyDown.setDown(false);
            switch (direction) {
                case FORWARD -> mc.options.keyUp.setDown(true);
                case FORWARD_LEFT -> {
                    mc.options.keyLeft.setDown(true);
                    mc.options.keyUp.setDown(true);
                }
                case FORWARD_RIGHT -> {
                    mc.options.keyRight.setDown(true);
                    mc.options.keyUp.setDown(true);
                }
                case LEFT -> mc.options.keyLeft.setDown(true);
                case RIGHT -> mc.options.keyRight.setDown(true);
                case BACK -> mc.options.keyDown.setDown(true);
                case BACK_LEFT -> {
                    mc.options.keyDown.setDown(true);
                    mc.options.keyLeft.setDown(true);
                }
                case BACK_RIGHT -> {
                    mc.options.keyDown.setDown(true);
                    mc.options.keyRight.setDown(true);
                }
                case NONE -> {

                }
            }
        }
    }

    static boolean isKeyPressed(KeyMapping keyBinding) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), ((MixinAccessorKeyBinding) keyBinding).getBoundKey().getValue());
    }
}
