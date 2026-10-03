package me.aleksilassila.litematica.printer.v26_1_2.guides.interaction;


import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.actions.*;
import me.aleksilassila.litematica.printer.v26_1_2.guides.placement.GeneralPlacementGuide;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.actions.UseItemActionImpl;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * @author IceTank
 * @since 08.03.2026
 */
public class WaterLogGuide extends GeneralPlacementGuide {
    boolean canWork = false;

    public WaterLogGuide(SchematicBlockState state) {
        super(state);
        canWork = state.targetState.getBlock() instanceof SimpleWaterloggedBlock;
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!canWork) return false;
        if (!(currentState.getBlock() instanceof SimpleWaterloggedBlock)) return false;

        if (currentState.getValue(BlockStateProperties.WATERLOGGED) == null
                || currentState.getValue(BlockStateProperties.WATERLOGGED) == targetState.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        }

//        if (!super.canExecute(player)) return false;
        List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.stream().allMatch(i -> i.is(Items.AIR)))
            return false;
        for (Direction side : getPossibleSides()) {
            if (canSeeBlockFace(player, new BlockHitResult(Vec3.atCenterOf(state.blockPos), side.getOpposite(), state.blockPos.relative(side), false))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        if (!canWork) return Collections.emptyList();
        return Collections.singletonList(new ItemStack(Items.WATER_BUCKET));
    }

    @Override
    public @Nullable PrinterPlacementContext getPlacementContext(LocalPlayer player) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return null;

        Vec3[] hitVecsToTryArray = getPossibleHitVecs();
        Vec3 playerEyePos = player.getEyePosition();

        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);
        int slot = getRequiredItemStackSlot(player);

        if (slot == -1) return null;

        for (Direction side : getPossibleSides()) {
            Vec3 hitVec = Vec3.atCenterOf(state.blockPos)
                    .add(new Vec3(side.step()).scale(0.5)); // Center of the block side face we are placing on

            for (Vec3 hitVecToTry : hitVecsToTryArray) {
                Vec3 multiplier = new Vec3(side.step());
                multiplier = new Vec3(
                        multiplier.x == 0 ? 1 : 0,
                        multiplier.y == 0 ? 1 : 0,
                        multiplier.z == 0 ? 1 : 0); // Offset from the Center of the block side face we are placing on by pre calculated values. This samples different points on that face.

                Vec3 blockHit = hitVec.add(hitVecToTry.multiply(multiplier));
                Vec3 lookDirection = blockHit.subtract(playerEyePos).normalize();
                Direction relativeDirection = Direction.getApproximateNearest(lookDirection.x, lookDirection.y, lookDirection.z);

                if (playerEyePos.distanceTo(blockHit) > LitematicaMixinMod.PRINTING_RANGE.getDoubleValue()) // Check if the hit vector is in range
                    continue;

                Vec3 lookVec = blockHit.subtract(playerEyePos).normalize(); // Look vector from the player's eye to the block hit vector
                Vec3 raycastEnd = playerEyePos.add(lookVec.scale(5)); // 5 block max distance
                ClipContext raycastContext = new ClipContext(playerEyePos, raycastEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult result = world.clip(raycastContext);
                if (result.getType() != HitResult.Type.BLOCK || !result.getBlockPos().equals(state.blockPos)) { // If we didn't hit a block, skip
                    continue;
                }
                if (result.getLocation().distanceTo(playerEyePos) > LitematicaMixinMod.PRINTING_RANGE.getDoubleValue()) { // Check if the hit result is in range
                    continue;
                }

                PrinterPlacementContext rayTraceContext = new PrinterPlacementContext(player, result, requiredItem, slot, relativeDirection, false);
                rayTraceContext.canStealth = true;
                rayTraceContext.isRaytrace = true;
                return rayTraceContext;
            }
        }
        return null;
    }

    @Override
    public @NotNull List<Action> execute(LocalPlayer player) {
        List<Action> actions = new ArrayList<>();

        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);
        if (requiredItem.isEmpty()) return actions;
        var ctx = getPlacementContext(player);
        if (ctx == null) return actions;

        ActionChain chain = new ActionChain();
        chain.addImmediateAction(new PrepareLook(ctx));
        chain.addNextTickAction(new PrepareAction(ctx));
        chain.addNextTickAction(new UseItemActionImpl(ctx));

        actions.add(chain);

        return actions;
    }
}
