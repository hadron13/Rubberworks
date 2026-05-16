package io.github.hadron13.rubberworks.blocks.sapper;

import com.simibubi.create.AllFluids;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.RubberworksLang;
import io.github.hadron13.rubberworks.register.RubberworksBlockEntities;
import io.github.hadron13.rubberworks.register.RubberworksBlocks;
import io.github.hadron13.rubberworks.register.RubberworksFluids;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.*;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.LeavesBlock.PERSISTENT;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class SapperBlockEntity extends KineticBlockEntity implements IHaveHoveringInformation {

    public static final int NUM_LEAVES = 5;
    public static final int NUM_SAPPERS = 5;
    public static final int EXTENSION_TIME = 10;

    private int extendedTicks = 0;
    private float sapTimer = 0;
    public int sapperState = 0;
    private boolean valid = false;
    private boolean cached = false;
    //registers a few leaves to keep track of
    public BlockPos[] leafPos = new BlockPos[NUM_LEAVES];
    public BlockPos[] otherSappers = new BlockPos[NUM_SAPPERS];
    public int otherSapperCounter = 0;
    public SmartFluidTankBehaviour tank;

    private FluidStack outputFluid;

    public static final int RETRACTED = 1;
    public static final int EXTENDING = 2;
    public static final int EXTENDED = 3;
    public static final int RETRACTING = 4;

    public SapperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        extendedTicks = 0;
        sapTimer = 0f;
        sapperState = RETRACTED;
        setLazyTickRate(10);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RubberworksBlockEntities.SAPPER.get(),
                (be, context) -> {
                    if (context == null || SapperBlock.hasPipeTowards(be.getLevel(), be.getBlockPos(), be.getBlockState(), context)){
                        return be.tank.getCapability();
                    }
                    return null;
                }
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        float speed = Math.abs(getSpeed());

        boolean onClient = level.isClientSide && !isVirtual();

        switch (sapperState) {
            case RETRACTED -> {
                if (valid && speed > 0 && !isTankFull()) {
                    setSapperState(EXTENDING);


                    sapTimer = 200*32;
                }
            }
            case EXTENDING -> {
                extendedTicks++;
                if (extendedTicks == EXTENSION_TIME)
                    setSapperState(EXTENDED);

            }
            case EXTENDED -> {
                sapTimer -= (speed - (0.0019f * speed * speed));
                if(!valid || isTankFull()){
                    setSapperState(RETRACTING);
                    break;
                }

                if (sapTimer <= 0) {
                    if(onClient){
                        BlockPos pos = getBlockPos();
                        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.HONEY_DRINK, SoundSource.AMBIENT, 0.5f, 1.0f, false);

                        return;
                    }

                    SmartFluidTank localTank = tank.getPrimaryHandler();

                    if(otherSapperCounter > 0){
                        float efficiency = 1.0f/(otherSapperCounter + 1.0f);
                        outputFluid.setAmount((int) (outputFluid.getAmount() * efficiency));
                    }

                    if(outputFluid != null)
                        localTank.fill(outputFluid, EXECUTE);

                    sapTimer = 200*32;
                    sendData();
                }
            }
            case RETRACTING -> {
                extendedTicks--;
                if (extendedTicks == 0)
                    setSapperState(RETRACTED);
            }
        }

    }
    public void setSapperState(int newState){
        if(level != null && level.isClientSide && !isVirtual())
            return;
        sapperState = newState;
        sendData();
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
    }
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean kineticTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        boolean fluidTooltip = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());

        if(getSpeed() != 0f && !valid)
            RubberworksLang.addHint(tooltip,"hint.sapper.invalid");

        if(otherSapperCounter > 0) {
            float efficiency = 1.0f/(otherSapperCounter + 1.0f);
            if(otherSapperCounter >= 5)
                efficiency = 0;

            RubberworksLang.addHint(tooltip, "hint.sapper.inefficient");
            RubberworksLang.translate("gui.sapper.efficiency")
                    .text(" " + (int)(efficiency*100.0f) + "%")
                    .forGoggles(tooltip);
        }


        return kineticTooltip || fluidTooltip;
    }

    public boolean isTankFull(){
        return tank.getPrimaryHandler().getFluidAmount() == 1000;
    }

    public void checkValidity(){
        if (level == null || !level.isLoaded(worldPosition) || level.isClientSide)
            return;
        BlockPos baseTrunkPos = getBlockPos().relative(getBlockState().getValue(HORIZONTAL_FACING).getOpposite(), 2);

        BlockState trunkType = level.getBlockState(baseTrunkPos);
        while (level.getBlockState(baseTrunkPos.below()) == trunkType) {
            baseTrunkPos = baseTrunkPos.below();
        }

        if(!TreeType.isValidLog(trunkType)) {
            cached = false;
            valid = false;
            return;
        }

        BlockPos trunkPos = baseTrunkPos.above();
        int treeHeight = 1;
        while (level.getBlockState(trunkPos) == trunkType) {
            trunkPos = trunkPos.above();
            treeHeight++;
        }
        if(treeHeight == 1){
            valid = false;
            cached = false;
            return;
        }

        int vertical = Math.min(level.getMaxBuildHeight() - worldPosition.getY(), treeHeight + 3);

        otherSapperCounter = 0;
        for(Direction dir : Iterate.horizontalDirections){
            Iterable<BlockPos> sapperArea = BlockPos.betweenClosed(baseTrunkPos.relative(dir, 2), baseTrunkPos.relative(dir, 2).above(treeHeight));
            for(BlockPos pos : sapperArea){
                BlockState blockState = level.getBlockState(pos);
                if(blockState.is(RubberworksBlocks.SAPPER)){
                    if(otherSapperCounter == NUM_SAPPERS)
                        continue;
                    if(pos.equals(worldPosition))
                        continue;
                    if(blockState.getValue(HORIZONTAL_FACING) != dir)
                        continue;

                    otherSappers[otherSapperCounter] = pos;
                    otherSapperCounter++;
                }
            }
        }
        sendData();

        if(cached){
            for (BlockPos pos: leafPos) {
                if(pos == null)
                    continue;
                if (!level.getBlockState(pos).is(BlockTags.LEAVES)) {
                    cached = false;
                    valid = false;
                    break;
                }
            }
        }
        if(cached) return;

        int leafCount = 0;
        Block leafType = null;

        Iterable<BlockPos> leafArea = BlockPos.betweenClosed(   baseTrunkPos.offset(3,0,3),
                baseTrunkPos.offset( -3, vertical, -3));

        for(BlockPos pos : leafArea){
            BlockState leafState = level.getBlockState(pos);

            if(leafState.isAir())
                continue;

            if(leafType == null && TreeType.isValidTree(trunkType, leafState) ) {
                leafType = leafState.getBlock();
            }

            if(leafState.getBlock() == leafType && (!leafState.hasProperty(PERSISTENT) || !leafState.getValue(PERSISTENT)) ){
                leafPos[leafCount] = pos.immutable();
                leafCount++;
                if(leafCount == NUM_LEAVES) {

                    List<RecipeHolder<SappingRecipe>> allRecipes = level.getRecipeManager().getAllRecipesFor(RubberworksRecipeTypes.SAPPING.getType());

                    Optional<SappingRecipe> matchingRecipes =
                            allRecipes.stream()
                                    .map(RecipeHolder::value)
                                    .filter(recipe -> trunkType.is(recipe.getLog()) && leafState.is(recipe.getLeaf()) )
                                    .findAny();
                    if(matchingRecipes.isEmpty())
                        return;

                    outputFluid = matchingRecipes.get().getFluidResults().get(0);

                    cached = true;
                    valid = true;
                    return;
                }
            }
        }
    }

    @Override
    public void lazyTick() {
        checkValidity();
    }

    public float getRenderedHeadOffset(float partialTicks) {

        final float startPos = 12 / 16f;
        final float finalPos = startPos + 6 / 16f;

        switch (sapperState){
            case RETRACTED  -> {return startPos;}
            case EXTENDED   -> {return finalPos;}
            case RETRACTING -> {partialTicks *= -1;}
        }
        float offset = (extendedTicks + partialTicks)/10f;

        float t =  1 - Mth.cos( offset * Mth.PI );
        t /= 2;

        return Mth.lerp(t, startPos, finalPos) ;
    }

    public float getRenderedHeadRotationSpeed(float partialTicks) {
        if (sapperState == EXTENDED) {
            return speed/ 2f;
        }
        return speed / 4f;
    }

    public void ponderFill(int ammount){
        SmartFluidTank localTank = tank.getPrimaryHandler();
        localTank.fill(new FluidStack(RubberworksFluids.RESIN.get(), ammount),
                EXECUTE);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        super.tickAudio();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {

        compound.putFloat("sapTime", sapTimer);
        compound.putInt("sapState", sapperState);
        compound.putInt("exTime", extendedTicks);
        compound.putBoolean("val", valid);
        if(clientPacket)
            compound.putInt("otherSappers", otherSapperCounter);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        sapTimer = compound.getFloat("sapTime");
        sapperState = compound.getInt("sapState");
        extendedTicks = compound.getInt("exTime");
        valid = compound.getBoolean("val");
        if(clientPacket)
            otherSapperCounter = compound.getInt("otherSappers");
        super.read(compound, registries, clientPacket);
    }

    public static class TreeType{
        public static HashSet<Block> logTypes = new HashSet<>();
        public static HashSet<Couple<Block>> treeTypes = new HashSet<>();
        public static void registerTree(Block log, Block leaf){
            logTypes.add(log);
            treeTypes.add(Couple.create(log, leaf));
        }
        public static boolean isValidLog(BlockState state){
            return logTypes.contains( state.getBlock() );
        }
        public static boolean isValidTree(BlockState log, BlockState leaf){
            return treeTypes.contains(Couple.create(log.getBlock(), leaf.getBlock()));
        }
    }
}