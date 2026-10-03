package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.Printer;
import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An old school guide where there are defined specific conditions
 * for player state depending on the block being placed.
 */
public class GeneralPlacementGuide extends PlacementGuide {
    protected static Vec3[] extendedHitVecs = new Vec3[]{
            new Vec3(-0.25, -0.25, -0.25),
            new Vec3(+0.25, -0.25, -0.25),
            new Vec3(-0.25, +0.25, -0.25),
            new Vec3(-0.25, -0.25, +0.25),
            new Vec3(+0.25, +0.25, -0.25),
            new Vec3(-0.25, +0.25, +0.25),
            new Vec3(+0.25, -0.25, +0.25),
            new Vec3(+0.25, +0.25, +0.25),
            new Vec3(-0.25, -0.49, -0.25), // 1/4 Just above the lower edge of a block. For carpets for instance as they are very thin
            new Vec3(+0.25, -0.49, -0.25), // 2/4
            new Vec3(-0.25, -0.49, +0.25), // 3/4
            new Vec3(+0.25, -0.49, +0.25) // 4/4
    };
    private PrinterPlacementContext contextCache = null;

    public GeneralPlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected List<Direction> getPossibleSides() {
        return Arrays.asList(Direction.values());
    }

    protected Optional<Direction> getLookDirection() {
        return Optional.empty();
    }

    protected boolean getRequiresSupport() {
        return false;
    }

    protected boolean getRequiresExplicitShift() {
        return false;
    }

    protected Vec3 getHitModifier(Direction validSide) {
        return new Vec3(0, 0, 0);
    }

    private Optional<Direction> getValidSide(SchematicBlockState state) {
        boolean printInAir = false; // LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides();

        if (sides.isEmpty()) {
            return Optional.empty();
        }

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            if (printInAir && !getRequiresSupport()) {
                return Optional.of(side);
            } else {
                SchematicBlockState neighborState = state.offset(side);

                if (getProperty(neighborState.currentState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE) {
                    validSides.add(side);
                    continue;
                }

                if (canBeClicked(neighborState.world, neighborState.blockPos) && // Handle unclickable grass for example
                        !neighborState.currentState.canBeReplaced())
                    validSides.add(side);
            }
        }

        for (Direction validSide : validSides) {
            if (!isInteractive(state.offset(validSide).currentState.getBlock())) {
                return Optional.of(validSide);
            }
        }

        return validSides.isEmpty() ? Optional.empty() : Optional.of(validSides.get(0));
    }

    protected Vec3[] getPossibleHitVecs() {
        return extendedHitVecs;
    }

    private List<Direction> getValidSides(SchematicBlockState state) {
        List<Direction> sides = getPossibleSides();

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            SchematicBlockState neighborState = state.offset(side);

            if (getProperty(neighborState.currentState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE) {
                validSides.add(side);
                continue;
            }

            if (canBeClicked(neighborState.world, neighborState.blockPos) && // Handle unclickable grass for example
                    !neighborState.currentState.canBeReplaced())
                validSides.add(side);
        }

        return validSides;
    }

    protected boolean getUseShift(SchematicBlockState state) {
        if (getRequiresExplicitShift()) return true;

        Direction clickSide = getValidSide(state).orElse(null);
        if (clickSide == null) return false;
        return isInteractive(state.offset(clickSide).currentState.getBlock());
    }

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!super.canExecute(player)) return false;
        if (!PrinterConfig.STRICT_BLOCK_FACE_CHECK.getBooleanValue()) {
            return true;
        }
        for (Direction side : getPossibleSides()) {
            if (canSeeBlockFace(player, new BlockHitResult(Vec3.atCenterOf(state.blockPos), side.getOpposite(), state.blockPos.relative(side), false))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable PrinterPlacementContext getPlacementContext(LocalPlayer player) {
        if (PrinterConfig.PRINTER_AIRPLACE.getBooleanValue()) {
            return getAirplaceContext(player);
        } else {
            return getContextByStrictLook(player);
        }
    }

    @Nullable
    public PrinterPlacementContext getContextByStrictLook(LocalPlayer player) {
        if (contextCache != null && !LitematicaMixinMod.DEBUG && !PrinterConfig.NO_PLACEMENT_CACHE.getBooleanValue() && contextCache.isRaytrace == PrinterConfig.RAYCAST.getBooleanValue())
            return contextCache;

        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);
        int slot = getRequiredItemStackSlot(player);

        if (slot == -1) return null;

        Vec3[] hitVecsToTryArray = getPossibleHitVecs();

        Vec3 playerEyePos = player.getEyePosition();
        // Direction relativeRotation = Direction.getEntityFacingOrder(player)[0];

        // for (Direction lookDirection : directionsToTry) {
        for (Direction side : getPossibleSides()) {
            BlockPos neighborPos = state.blockPos.relative(side);

            // Check if the block face is visible. Prevents the printer from trying to place blocks on the backside of other blocks
            if (PrinterConfig.STRICT_BLOCK_FACE_CHECK.getBooleanValue()) {
                if (!canSeeBlockFace(player, new BlockHitResult(Vec3.atCenterOf(state.blockPos), side.getOpposite(), neighborPos, false))) {
                    continue;
                }
            }

            BlockState neighborState = state.world.getBlockState(neighborPos);
            boolean requiresShift = getRequiresExplicitShift() || isInteractive(neighborState.getBlock());

            if (!canBeClicked(state.world, neighborPos) || // Handle unclickable grass for example
                    neighborState.canBeReplaced())
                continue;

            Vec3 hitVec = Vec3.atCenterOf(state.blockPos)
                    .add(new Vec3(side.step()).scale(0.5)); // Center of the block side face we are placing on

            // Now we bring on the big guns, brute force the hit vector until we find a solution that directly hits the neighbor block without obstruction
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

                if (PrinterConfig.RAYCAST.getBooleanValue() && mc.level != null && mc.player != null) {
                    Vec3 lookVec = blockHit.subtract(playerEyePos).normalize(); // Look vector from the player's eye to the block hit vector
                    Vec3 raycastEnd = playerEyePos.add(lookVec.scale(5)); // 5 block max distance
                    ClipContext raycastContext = new ClipContext(playerEyePos, raycastEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                    BlockHitResult result = mc.level.clip(raycastContext);
                    if (result.getType() != HitResult.Type.BLOCK) { // If we didn't hit a block, skip
                        continue;
                    }

                    if (result.getBlockPos().equals(neighborPos)) {
                        if (PrinterConfig.RAYCAST_STRICT_BLOCK_HIT.getBooleanValue()) { // Check if the right side was hit
                            Direction hitSide = result.getDirection();
                            if (hitSide.getOpposite() != side) {
                                continue;
                            }
                        }
                        if (result.getLocation().distanceTo(playerEyePos) > LitematicaMixinMod.PRINTING_RANGE.getDoubleValue()) { // Check if the hit result is in range
                            continue;
                        }
                        BlockHitResult hitResult = new BlockHitResult(blockHit, side.getOpposite(), neighborPos, false);
                        PrinterPlacementContext rayTraceContext = new PrinterPlacementContext(player, hitResult, requiredItem, slot, relativeDirection, requiresShift);
                        rayTraceContext.canStealth = true;
                        rayTraceContext.isRaytrace = true;
                        BlockState resultState = getRequiredItemAsBlock(player)
                                .orElse(targetState.getBlock())
                                .getStateForPlacement(rayTraceContext);

                        if (resultState != null && correctObserverPlacement(targetState, resultState) && (statesEqual(resultState, targetState) || correctChestPlacement(targetState, resultState))) {
                            contextCache = rayTraceContext;
                            return rayTraceContext;
                        }
                    }
                    continue;
                } else /* No Raycast */ {
                    BlockHitResult hitResult = new BlockHitResult(blockHit, side.getOpposite(), neighborPos, false);
                    PrinterPlacementContext context = new PrinterPlacementContext(player, hitResult, requiredItem, slot, relativeDirection, requiresShift);
                    context.canStealth = true;
                    BlockState result = getRequiredItemAsBlock(player)
                            .orElse(targetState.getBlock())
                            .getStateForPlacement(context); // FIXME torch shift clicks another torch and getPlacementState is the clicked block, which is true

                    if (result != null && correctObserverPlacement(targetState, result) && (statesEqual(result, targetState) || correctChestPlacement(targetState, result))) {
                        contextCache = context;
                        return context;
                    }
                }
            }
        }
        // }

        return null;
    }

    protected static Direction[] directionsToTry = new Direction[]{
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST,
            Direction.UP,
            Direction.DOWN
    };

    /**
     * Produces a PrinterPlacementContext for air placing blocks. The HitResult is brute forced by trying all sides and
     * various hit positions inside the block volume. The HitResult block places against the neighbor block in the given
     * side direction. To get the target block position we offset the {@link BlockHitResult#getBlockPos()} by the side
     * direction.
     * @param player the player
     * @return the PrinterPlacementContext or null if no valid context was found
     */
    @Nullable
    public PrinterPlacementContext getAirplaceContext(LocalPlayer player) {
        if (contextCache != null && !LitematicaMixinMod.DEBUG) return contextCache;

        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);
        int slot = getRequiredItemStackSlot(player);

        if (slot == -1) return null;

        if (Printer.inactivityCounter <= PrinterConfig.PRINTER_MIN_INACTIVE_TIME_AIR_PLACE.getIntegerValue()) {
            return null;
        }

        // We'll brute force: sides (including horizontal) + hit positions inside the target block volume
        // to emulate realistic clicks that yield correct orientation for special blocks (hoppers, froglights, basalt, slabs).

        // First quick attempt: original center (UP side) (fast path)
        {
            BlockHitResult hr = new BlockHitResult(Vec3.atCenterOf(state.blockPos), Direction.UP, state.blockPos, true);
            PrinterPlacementContext quick = new PrinterPlacementContext(player, hr, requiredItem, slot, null, false);
            BlockState res = getRequiredItemAsBlock(player).orElse(targetState.getBlock()).getStateForPlacement(quick);
            if (res != null && correctObserverPlacement(targetState, res) && statesEqual(res, targetState)) {
                contextCache = quick;
                quick.isAirPlace = true;
                return quick;
            }
        }

        // Hit offsets inside block relative to its min corner (0..1). Include lower y (<0.5) for bottom slabs, and center variations.
        double[] ySamples = new double[]{0.25, 0.5, 0.75};
        double[] xzSamples = new double[]{0.25, 0.5, 0.75};

        for (Direction lookDirection : directionsToTry) {
            for (Direction side : getPossibleSides()) {
                // For each sample point on the face or interior adjust the hit vector.
                for (double y : ySamples) {
                    for (double x : xzSamples) {
                        for (double z : xzSamples) {
                            // Start with raw interior sample
                            Vec3 sample = new Vec3(x, y, z);
                            Vec3 hitVec = Vec3.atLowerCornerOf(state.blockPos).add(sample);

                            // Ensure the hitVec lies on the correct face for the side: project coordinate component to face plane center
                            hitVec = switch (side) {
                                case UP -> new Vec3(hitVec.x, state.blockPos.getY() + 1 - 1e-4, hitVec.z);
                                case DOWN -> new Vec3(hitVec.x, state.blockPos.getY() + 1e-4, hitVec.z);
                                case NORTH -> new Vec3(hitVec.x, hitVec.y, state.blockPos.getZ() + 1e-4);
                                case SOUTH -> new Vec3(hitVec.x, hitVec.y, state.blockPos.getZ() + 1 - 1e-4);
                                case WEST -> new Vec3(state.blockPos.getX() + 1e-4, hitVec.y, hitVec.z);
                                case EAST -> new Vec3(state.blockPos.getX() + 1 - 1e-4, hitVec.y, hitVec.z);
                            };

                            BlockHitResult hitResult = new BlockHitResult(hitVec, side.getOpposite(), state.blockPos, true);
                            PrinterPlacementContext context = new PrinterPlacementContext(player, hitResult, requiredItem, slot, lookDirection, false);
                            BlockState result = getRequiredItemAsBlock(player)
                                    .orElse(targetState.getBlock())
                                    .getStateForPlacement(context);
                            if (result != null && correctObserverPlacement(targetState, result) && statesEqual(result, targetState)) {
                                contextCache = context;
                                context.isAirPlace = true;
                                return context;
                            }
                        }
                    }
                }
            }
        }

//        if (PrinterConfig.PRINTER_AIRPLACE_FLOATING_ONLY.getBooleanValue() && !isInAir(state.blockPos)) {
//            return null;
//        }
//        if (Printer.inactivityCounter < PrinterConfig.PRINTER_MIN_INACTIVE_TIME_AIR_PLACE.getIntegerValue()) {
//            return null;
//        }
//        if (PrinterConfig.PRINTER_AIRPLACE_RANGE.getDoubleValue() < 0 || player.getEyePosition().distanceTo(Vec3.atCenterOf(state.blockPos)) > PrinterConfig.PRINTER_AIRPLACE_RANGE.getDoubleValue()) {
//            return null;
//        }

        return null;
    }

    private boolean correctChestPlacement(BlockState targetState, BlockState result) {
        if (targetState.hasProperty(ChestBlock.TYPE) && result.hasProperty(ChestBlock.TYPE) && result.getValue(ChestBlock.FACING) == targetState.getValue(ChestBlock.FACING)) {
            ChestType targetChestType = targetState.getValue(ChestBlock.TYPE);
            ChestType resultChestType = result.getValue(ChestBlock.TYPE);

            return targetChestType != ChestType.SINGLE && resultChestType == ChestType.SINGLE;
        }

        return false;
    }

    /**
     * Returns true if the observer is placed correctly or if the config printerPlaceObserversLast is set to false
     * @param targetState the target state of the block being placed
     * @param result the result state of the block being placed
     * @return true if the observer is placed correctly, false otherwise
     */
    private boolean correctObserverPlacement(BlockState targetState, BlockState result) {
        if (!PrinterConfig.PRINTER_PLACE_OBSERVERS_LAST.getBooleanValue()) return true; // If the config is set to place observers last, we don't need to check this

        if (result.getBlock() != Blocks.OBSERVER) return true;
        if (targetState.getBlock() != Blocks.OBSERVER) return true;

        Direction facing = result.getValue(BlockStateProperties.FACING);
        if (facing == null) return false;

        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) return false;
        if (mc.level == null) return false;

        if (schematicWorld.getBlockState(state.blockPos.relative(facing)).isAir()) {
            return true;
        } else {
            return !mc.level.getBlockState(state.blockPos.relative(facing)).isAir();
        }
    }

    protected boolean canSeeBlockFace(LocalPlayer player, BlockHitResult hitResult) {
        // Draw a line between the player pos and the block pos and check if the block side is visible
        // BlockPos targetPos = state.blockPos;

        Direction side = hitResult.getDirection();
        BlockPos blockPos = hitResult.getBlockPos();
        Vec3 playerEyePos = player.getEyePosition();
        switch (side) {
            case UP:
                if (blockPos.getY() + 1 > playerEyePos.y) return false;
                break;
            case DOWN:
                if (blockPos.getY() < playerEyePos.y) return false;
                break;
            case NORTH:
                if (blockPos.getZ() < playerEyePos.z) return false;
                break;
            case SOUTH:
                if (blockPos.getZ() + 1 > playerEyePos.z) return false;
                break;
            case EAST:
                if (blockPos.getX() + 1 > playerEyePos.x) return false;
                break;
            case WEST:
                if (blockPos.getX() < playerEyePos.x) return false;
                break;
        }
        return true;
    }
}
