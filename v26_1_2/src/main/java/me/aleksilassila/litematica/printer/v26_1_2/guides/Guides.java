package me.aleksilassila.litematica.printer.v26_1_2.guides;

import me.aleksilassila.litematica.printer.v26_1_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v26_1_2.guides.interaction.*;
import me.aleksilassila.litematica.printer.v26_1_2.guides.placement.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class Guides {
    protected final static ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = new ArrayList<>();

    @SafeVarargs
    protected static void registerGuide(Class<? extends Guide> guideClass, Class<? extends Block>... blocks) {
        guides.add(Pair.of(guideClass, blocks));
    }

    static {
//        registerGuide(SkipGuide.class, SignBlock.class, SkullBlock.class, BannerBlock.class);

        registerGuide(RotatingBlockGuide.class, AbstractSkullBlock.class, SignBlock.class, AbstractBannerBlock.class);
        registerGuide(FacingBlockGuide.class, StairBlock.class, GlazedTerracottaBlock.class);
        registerGuide(SlabGuide.class, SlabBlock.class);
        registerGuide(TorchGuide.class, TorchBlock.class, WallTorchBlock.class);
        registerGuide(FarmlandGuide.class, FarmlandBlock.class);
        registerGuide(TillingGuide.class, FarmlandBlock.class);
        registerGuide(RailGuesserGuide.class, BaseRailBlock.class);
        registerGuide(ChestGuide.class, ChestBlock.class);
        registerGuide(FlowerPotGuide.class, FlowerPotBlock.class);
        registerGuide(FlowerPotFillGuide.class, FlowerPotBlock.class);

        registerGuide(FallingBlockGuide.class, FallingBlock.class);
        registerGuide(BlockIndifferentGuesserGuide.class, BambooStalkBlock.class, BigDripleafStemBlock.class, BigDripleafBlock.class,
                TwistingVinesPlantBlock.class, TripWireBlock.class, MushroomBlock.class, MultifaceBlock.class);

        registerGuide(CampfireExtinguishGuide.class, CampfireBlock.class);
        registerGuide(LightCandleGuide.class, AbstractCandleBlock.class);
        registerGuide(EnderEyeGuide.class, EndPortalFrameBlock.class);

        // Catch most blocks
        registerGuide(PropertySpecificGuesserGuide.class);
//        registerGuide(LogGuide.class);

        // Anything that replaces or changes stuff after placement
        registerGuide(CycleStateGuide.class,
                DoorBlock.class, FenceGateBlock.class, TrapDoorBlock.class,
                LeverBlock.class,
                RepeaterBlock.class, ComparatorBlock.class, NoteBlock.class);
        registerGuide(BlockReplacementGuide.class, SnowLayerBlock.class, SeaPickleBlock.class, CandleBlock.class, SlabBlock.class);
        registerGuide(WaterLogGuide.class);
        registerGuide(LogStrippingGuide.class);
    }

    public ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> getGuides() {
        return guides;
    }

    public Guide[] getInteractionGuides(SchematicBlockState state) {
        ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = getGuides();

        ArrayList<Guide> applicableGuides = new ArrayList<>();
        for (Pair<Class<? extends Guide>, Class<? extends Block>[]> guidePair : guides) {
            try {
                if (guidePair.getRight().length == 0) {
                    applicableGuides.add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    continue;
                }

                for (Class<? extends Block> clazz : guidePair.getRight()) {
                    if (clazz.isInstance(state.targetState.getBlock())) {
                        applicableGuides.add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return applicableGuides.toArray(Guide[]::new);
    }
}
