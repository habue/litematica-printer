package me.aleksilassila.litematica.printer.v26_1_2.guides.placement;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.actions.*;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import me.aleksilassila.litematica.printer.v26_1_2.guides.Guide;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.actions.AirPlaceAction;
import me.aleksilassila.litematica.printer.v26_1_2.implementation.actions.InteractActionImpl;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Guide that clicks its neighbors to create a placement in target position.
 */
abstract public class PlacementGuide extends Guide {
    Minecraft mc = Minecraft.getInstance();

    public PlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected ItemStack getBlockItem(BlockState state) {
        return state.getBlock().asItem().getDefaultInstance();
    }

    protected Optional<Block> getRequiredItemAsBlock(LocalPlayer player) {
        ItemStack requiredItem = getRequiredItem(player).stream().findFirst().orElse(ItemStack.EMPTY);

        if (requiredItem.isEmpty()) {
            return Optional.empty();
        } else {
            if (requiredItem.getItem() instanceof BlockItem)
                return Optional.of(((BlockItem) requiredItem.getItem()).getBlock());
            else return Optional.empty();
        }
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(getBlockItem(state.targetState));
    }

    abstract protected boolean getUseShift(SchematicBlockState state);

    @Nullable
    abstract public PrinterPlacementContext getPlacementContext(LocalPlayer player);

    @Override
    public boolean canExecute(LocalPlayer player) {
        if (!super.canExecute(player)) return false;

        if (targetState.getBlock() == currentState.getBlock()) {
            return false;
        }

        List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.stream().allMatch(i -> i.is(Items.AIR)))
            return false;

        BlockPlaceContext ctx = getPlacementContext(player);
        if (ctx == null || !ctx.canPlace()) return false;

        BlockState resultState = getRequiredItemAsBlock(player)
                .orElse(targetState.getBlock())
                .getStateForPlacement(ctx);

        if (resultState != null) {
            if (collidesWithPlayer(resultState)) {
                if (PrinterConfig.isDebug()) {
                    System.out.println("Block collides with player. Not placing.");
                }
                return false;
            }
            if (!resultState.canSurvive(state.world, state.blockPos)) return false;
            return !(currentState.getBlock() instanceof LiquidBlock) || canPlaceInWater(resultState);
        } else {
            return false;
        }

    }

    public boolean isInAir(BlockPos pos) {
        if (mc.level == null) return false;
        for (Direction dir : Direction.values()) {
            if (!mc.level.getBlockState(pos.relative(dir)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean collidesWithPlayer(BlockState blockState) {
        if (mc.player == null || mc.level == null) return true;

        VoxelShape shape = blockState.getCollisionShape(state.schematic, state.blockPos);
        if (shape.isEmpty()) return false;
        shape = shape.move(state.blockPos.getX(), state.blockPos.getY(), state.blockPos.getZ());
        AABB playerShape = mc.player.getBoundingBox();
        return Shapes.joinIsNotEmpty(Shapes.create(playerShape), shape, BooleanOp.AND);
    }

    @Override
    public @NotNull List<Action> execute(LocalPlayer player) {
        List<Action> actions = new ArrayList<>();
        PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null) return actions;
        ActionChain actionChain = new ActionChain();

        if (ctx.isAirPlace) {
            if (ctx.lookDirection != null) {
                if (PrinterConfig.PRINTER_GRIM_ROTATION.getBooleanValue()) {
                    actionChain.addNextTickAction(new PrepareLook(ctx));
                } else {
                    actionChain.addImmediateAction(new PrepareLook(ctx));
                }
            }
            actionChain.addNextTickAction(new PrepareAction(ctx));
            actionChain.addNextTickAction(new AirPlaceAction(ctx));
            actions.add(actionChain);
            return actions;
        } else {
            if (PrinterConfig.PRINTER_AIRPLACE.getBooleanValue() && PrinterConfig.PRINTER_AIRPLACE_ONLY.getBooleanValue()) {
                return actions;
            }
        }

        boolean shiftDown = mc.player.input.keyPresses.shift();
        actionChain.addImmediateAction(new PrepareLook(ctx));
        if (ctx.shouldSneak && !shiftDown) actionChain.addImmediateAction(new PresShift());
        actionChain.addNextTickAction(new PrepareAction(ctx));
        actionChain.addNextTickAction(new InteractActionImpl(ctx));
        if (ctx.shouldSneak && !shiftDown) actionChain.addNextTickAction(new ReleaseShiftAction());
        actions.add(actionChain);

        return actions;
    }

    protected static boolean canBeClicked(Level world, BlockPos pos) {
        return getOutlineShape(world, pos) != Shapes.empty() && !(world.getBlockState(pos).getBlock() instanceof SignBlock); // FIXME signs
    }

    private static VoxelShape getOutlineShape(Level world, BlockPos pos) {
        return world.getBlockState(pos).getShape(world, pos);
    }

    public boolean isInteractive(Block block) {
        for (Class<?> clazz : interactiveBlocks) {
            if (clazz.isInstance(block)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceInWater(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof SimpleWaterloggedBlock) {
            return true;
        } else if (!(block instanceof DoorBlock) && !(blockState.getBlock() instanceof SignBlock) && !blockState.is(Blocks.LADDER) && !blockState.is(Blocks.SUGAR_CANE) && !blockState.is(Blocks.BUBBLE_COLUMN)) {
//            Material material = blockState.getMaterial();
//            if (material != Material.PORTAL && material != Material.STRUCTURE_VOID && material != Material.UNDERWATER_PLANT && material != Material.REPLACEABLE_UNDERWATER_PLANT) {
//                return material.blocksMotion();
//            } else {
//                return true;
//            }
            return blockState.blocksMotion();
        }

        return true;
    }
}
