package me.aleksilassila.litematica.printer.v26_1_2.implementation;

import me.aleksilassila.litematica.printer.v26_1_2.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class PrinterPlacementContext extends BlockPlaceContext {
    public final @Nullable Direction lookDirection;
    public final boolean shouldSneak;
    public final BlockHitResult hitResult;
    public final int requiredItemSlot;
    public boolean canStealth = false;
    public boolean isRaytrace = false;
    public boolean isAirPlace = false;

    public PrinterPlacementContext(Player player, BlockHitResult hitResult, ItemStack requiredItem, int requiredItemSlot) {
        this(player, hitResult, requiredItem, requiredItemSlot, null, false);
    }

    public PrinterPlacementContext(Player player, BlockHitResult hitResult, ItemStack requiredItem, int requiredItemSlot, @Nullable Direction lookDirection, boolean requiresSneaking) {
        super(player, InteractionHand.MAIN_HAND, requiredItem, hitResult);

        this.lookDirection = lookDirection;
        this.shouldSneak = requiresSneaking;
        this.hitResult = hitResult;
        this.requiredItemSlot = requiredItemSlot;
    }

    @Override
    public Direction getNearestLookingDirection() {
        return lookDirection == null ? super.getNearestLookingDirection() : lookDirection;
    }

    @Override
    public Direction getNearestLookingVerticalDirection() {
        if (lookDirection != null && lookDirection.getOpposite() == super.getNearestLookingVerticalDirection())
            return lookDirection;
        return super.getNearestLookingVerticalDirection();
    }

    @Override
    public Direction getHorizontalDirection() {
        if (lookDirection == null || !lookDirection.getAxis().isHorizontal()) return super.getHorizontalDirection();

        return lookDirection;
    }

    public BlockPos getBlockPos() {
        return hitResult.getBlockPos();
    }

    public Direction getDirection() {
        return hitResult.getDirection();
    }

    public Vec3 getHitPos() {
        return hitResult.getLocation();
    }

    @Override
    public boolean canPlace() {
        if (!isAirPlace) {
            return super.canPlace();
        }
        if (!super.canPlace()) {
            return false;
        }
        BlockState currentState = this.getLevel().getBlockState(hitResult.getBlockPos());
        if (this.getPlayer().getEyePosition().distanceTo(Vec3.atCenterOf(hitResult.getBlockPos())) > PrinterConfig.PRINTER_AIRPLACE_RANGE.getDoubleValue()) {
            return false;
        }
        // Wrong state and not replaceable
        if (!currentState.canBeReplaced() && !(currentState.getBlock() instanceof FireBlock)) {
            return false;
        } else {
            // Replaceable block. Check fluid source block replacement config
            if (currentState.getBlock() instanceof LiquidBlock && !LitematicaMixinMod.REPLACE_FLUIDS_SOURCE_BLOCKS.getBooleanValue()) {
                // Only allow replacing fluid source blocks if the config is enabled
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "PrinterPlacementContext{" +
                "lookDirection=" + lookDirection +
                ", requiresSneaking=" + shouldSneak +
                ", blockPos=" + hitResult.getBlockPos() +
                ", side=" + hitResult.getDirection() +
//                ", hitVec=" + hitResult +
                '}';
    }
}
