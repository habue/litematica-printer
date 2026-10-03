package me.aleksilassila.litematica.printer.v26_1_2;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v26_1_2.actions.Action;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.guides.Guide;
import me.aleksilassila.litematica.printer.v26_1_2.guides.Guides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Printer {
    public static final Logger logger = LogManager.getLogger("litematica-printer");
    @NotNull
    public final LocalPlayer player;
    Minecraft mc = Minecraft.getInstance();

    public final ActionHandler actionHandler;

    private final Guides interactionGuides = new Guides();
    public static final InventoryManager inventoryManager = InventoryManager.getInstance();
    public static int inactivityCounter = 0;
    static final LinkedList<BlockTimeout> blockPosTimeout = new LinkedList<>();
    int delayCounter = 0;
    @Nullable
    public static Vec2 lastRotation = null;

    public Printer(@NotNull Minecraft client, @NotNull LocalPlayer player) {
        this.player = player;

        this.actionHandler = new ActionHandler(client, player);
    }

    public void onMiddleClick() {
        if (mc.level == null || mc.player == null) return;
        BlockPos pos = RayTraceUtils.getSchematicWorldTraceIfClosest(mc.level, mc.player, 6.0);

        if (pos != null) {
            WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
            if (world == null) return;
            inventoryManager.pickSlot(world, player, pos);
        }
    }

    public boolean onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        blockPosTimeout.forEach((entry) -> entry.timer--);
        blockPosTimeout.removeIf((entry) -> entry.timer <= 0);

        // If the inactivityCounter is greater than the inactive snap back value, then set the lastRotation to the current rotation
        // This is used to snap back to the last rotation when the player is inactive
        inactivityCounter++;

        if (worldSchematic == null) return false;

        if (PrinterConfig.TICK_DELAY.getIntegerValue() != 0 && delayCounter < PrinterConfig.TICK_DELAY.getIntegerValue()) {
            delayCounter++;
            return false;
        } else {
            delayCounter = 0;
        }

        if (!LitematicaMixinMod.PRINT_MODE.getBooleanValue() && !LitematicaMixinMod.PRINT.getKeybind().isPressed())
            return false;

        Abilities abilities = player.getAbilities();
        if (!abilities.mayBuild)
            return false;

        if (PrinterConfig.STOP_ON_MOVEMENT.getBooleanValue() && player.getDeltaMovement().length() > 0.1)
            return false; // Stop if the player is moving
        if (PrinterConfig.PRINTER_DISABLE_IN_GUIS.getBooleanValue()) {
            if (mc.screen != null && !(mc.screen instanceof ChatScreen)) return false;
        }

        List<BlockPos> positions = getReachablePositions();

        if (PrinterConfig.BLOCK_TIMEOUT.getIntegerValue() != 0) {
            positions = positions.stream().filter((pos) -> blockPosTimeout.stream().noneMatch((entry) -> entry.pos.equals(pos))).toList(); // From block timeout. Don't place already placed blocks.
        }

        findBlock:
        for (BlockPos position : positions) {
            SchematicBlockState state = new SchematicBlockState(player.level(), worldSchematic, position);
            if (state.targetState.equals(state.currentState) || state.targetState.isAir()) continue;

            Guide[] guides = interactionGuides.getInteractionGuides(state);

            for (Guide guide : guides) {
                if (guide.canExecute(player)) {
                    List<Action> actions = guide.execute(player);
                    actionHandler.addActions(actions.toArray(Action[]::new));
                    return true;
                }
                if (guide.skipOtherGuides()) continue findBlock;
            }
        }

        return false;
    }

    private List<BlockPos> getBlocksPlayerOccupied() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        BlockPos playerPos = player.blockPosition();
        int blocksHeightOccupied = (int) Math.ceil(player.position().y + player.getBbHeight() - playerPos.getY());

        positions.add(player.blockPosition());
        positions.add(player.blockPosition().above());
        if (blocksHeightOccupied > 2) {
            positions.add(playerPos.above(2));
        }
        if (Math.floor(player.position().x + player.getBbWidth() / 2) > playerPos.getX()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.above(i).east());
            }
        }
        if ((player.position().x - player.getBbWidth() / 2) < playerPos.getX()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.above(i).west());
            }
        }
        if (Math.floor(player.position().z + player.getBbWidth() / 2) > playerPos.getZ()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.above(i).south());
            }
        }
        if ((player.position().z - player.getBbWidth() / 2) < playerPos.getZ()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.above(i).north());
            }
        }
        return positions.stream().distinct().toList();
    }

    private List<BlockPos> getReachablePositions() {
        List<BlockPos> playerOccupied = getBlocksPlayerOccupied();
        int maxReach = (int) Math.ceil(LitematicaMixinMod.PRINTING_RANGE.getDoubleValue());
        double maxReachSquared = Mth.square(LitematicaMixinMod.PRINTING_RANGE.getDoubleValue());

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = -maxReach; y < maxReach + 1; y++) {
            for (int x = -maxReach; x < maxReach + 1; x++) {
                for (int z = -maxReach; z < maxReach + 1; z++) {
                    BlockPos blockPos = player.blockPosition().north(x).west(z).above(y);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(blockPos)) continue;
                    if (this.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockPos)) > maxReachSquared) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }

        return positions.stream()
//                .filter(p -> playerOccupied.stream().noneMatch(p::equals))
                .sorted((a, b) -> {
                    double aDistance = this.player.position().distanceToSqr(Vec3.atCenterOf(a));
                    double bDistance = this.player.position().distanceToSqr(Vec3.atCenterOf(b));
                    return Double.compare(aDistance, bDistance);
                }).toList();
    }

    public static void addTimeout(BlockPos pos) {
        blockPosTimeout.add(new BlockTimeout(pos, PrinterConfig.BLOCK_TIMEOUT.getIntegerValue()));
    }

    public void rotate(float yaw, float pitch) {
        LitematicaMixinMod.freeLook.ticksSinceLastRotation = 0;
        this.player.setYRot(yaw);
        this.player.setXRot(pitch);
    }

    public static class BlockTimeout {
        int timer = 0;
        BlockPos pos;

        public BlockTimeout(BlockPos pos, int timer) {
            this.pos = pos;
            this.timer = timer;
        }
    }
}
