package io.github.hadron13.rubberworks.blocks.compressor;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.sound.SoundScapes;
import io.github.hadron13.rubberworks.RubberworksLang;
import io.github.hadron13.rubberworks.blocks.sapper.SapperBlock;
import io.github.hadron13.rubberworks.register.RubberworksBlockEntities;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.*;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class CompressorBlockEntity extends KineticBlockEntity {

    public SmartFluidTankBehaviour tank;
    public SmartInventory output;

    protected IItemHandlerModifiable itemCapability;

    public int timer;
    public static final int OUTPUT_SLOTS = 3;
    public List<Integer> spoutputIndex;
    public CompressingRecipe recipe;

    public static final int OUTPUT_ANIMATION_TIME = 10;
    public List<IntAttached<ItemStack>>  visualizedOutputItems;


    public CompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        output = new SmartInventory(OUTPUT_SLOTS, this)
                .forbidInsertion()
                .withMaxStackSize(64);

        itemCapability = new InvWrapper(output);
        spoutputIndex = new ArrayList<>();
        visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());
        timer = -1;
        recipe = null;
    }


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RubberworksBlockEntities.COMPRESSOR.get(),
                (be, context) -> {
                    if (context == null || CompressorBlock.hasPipeTowards(be.level, be.worldPosition, be.getBlockState(), context) ){
                        return be.tank.getCapability();
                    }
                    return null;
                }
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RubberworksBlockEntities.COMPRESSOR.get(),
                (be, context) -> {
                    if (context == null || !CompressorBlock.hasPipeTowards(be.level, be.worldPosition, be.getBlockState(), context)){
                        return be.itemCapability;
                    }
                    return null;
                }
        );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, output);
    }

    @Override
    public void tick() {
        super.tick();
        if(level==null)
            return;

        if(level.isClientSide){
            visualizedOutputItems.forEach(IntAttached::decrement);
            visualizedOutputItems.removeIf(IntAttached::isOrBelowZero);
        }

        Direction dir = getBlockState().getValue(HORIZONTAL_FACING);
        if(!level.isClientSide && !spoutputIndex.isEmpty() && BasinBlock.canOutputTo(level, getBlockPos(), dir)) {
            for (int i = 0; i < spoutputIndex.size(); i++) {
                ItemStack item = output.getItem(spoutputIndex.get(i));
                if (item.isEmpty()) {
                    spoutputIndex.remove(i);
                    continue;
                }

                BlockEntity be = level.getBlockEntity(worldPosition.below()
                        .relative(dir));

                FilteringBehaviour filter = null;
                InvManipulationBehaviour inserter = null;
                if (be != null) {
                    filter = BlockEntityBehaviour.get(level, be.getBlockPos(), FilteringBehaviour.TYPE);
                    inserter = BlockEntityBehaviour.get(level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
                }

                IItemHandler targetInv = be == null ? null
                        : Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), dir.getOpposite()))
                        .orElse(inserter == null ? null : inserter.getInventory());

                if (targetInv == null)
                    continue;
                if (!ItemHandlerHelper.insertItemStacked(targetInv, item, true)
                        .isEmpty())
                    continue;
                if (filter != null && !filter.test(item))
                    continue;
                ItemHandlerHelper.insertItemStacked(targetInv,
                        output.extractItem(spoutputIndex.get(i), 64, false), false);
                visualizedOutputItems.add(IntAttached.withZero(item));
            }
        }

        if (!validSpeed())
            return;
        for (int i = 0; i < output.getSlots(); i++)
            if (output.getStackInSlot(i)
                    .getCount() == output.getSlotLimit(i))
                return;

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (timer <= 0) {
                if (level.isClientSide) {
                    float pitch = Mth.clamp((Math.abs(getSpeed()) / 256f) + .35f, .75f, 0.85f);
                    BlockPos pos = getBlockPos();

                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                            AllSoundEvents.STEAM.getMainEvent(), SoundSource.AMBIENT,0.5f, pitch, false);
                    return;
                }

                process();
            }
            return;
        }
        if (tank.getPrimaryHandler().isEmpty())
            return;

        if (!CompressingRecipe.match(this, recipe)) {
            Optional<CompressingRecipe> newRecipe = RubberworksRecipeTypes.COMPRESSING.find(this, level);
            if (newRecipe.isEmpty()) {
                timer = 100;
                sendData();
            } else {
                recipe = newRecipe.get();
                timer = recipe.getProcessingDuration();
                sendData();
            }
            return;
        }

        timer = recipe.getProcessingDuration();
        sendData();
    }

    public boolean validSpeed(){
        switch (getBlockState().getValue(HORIZONTAL_FACING)){
            case EAST, NORTH -> {
                return speed > 0;
            }
            case WEST, SOUTH -> {
                return speed < 0;
            }
        }
        return false;
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        super.tickAudio();

        if (!validSpeed())
            return;
        if(recipe == null)
            return;
        if(level.random.nextInt(5) != 1)
            return;

        float pitch = Mth.clamp((Math.abs(getSpeed()) / 256f) + .45f, .85f, .95f);
        BlockPos pos = getBlockPos();

        SoundScapes.play(SoundScapes.AmbienceGroup.MILLING, worldPosition, pitch);

    }

    public BlazeBurnerBlock.HeatLevel getHeat(){
        assert level != null;
        return BasinBlockEntity.getHeatLevelOf(level.getBlockState(getBlockPos().below()));
    }

    private void process() {

        if (!CompressingRecipe.match(this, recipe)) {
            Optional<CompressingRecipe> newRecipe = RubberworksRecipeTypes.COMPRESSING.find(this, level);
            if (newRecipe.isEmpty())
                return;
            recipe = newRecipe.get();
        }
        int usedAmmount = recipe.getFluidIngredients().get(0).amount();
        tank.getPrimaryHandler().drain(usedAmmount, EXECUTE);

        output.allowInsertion();
        recipe.rollResults(level.getRandom())
                .forEach(stack -> ItemHandlerHelper.insertItemStacked(output, stack, false));
        output.forbidInsertion();
        for(int i = 0; i < output.getSlots(); i++) {
            if(!spoutputIndex.contains(i))
                spoutputIndex.add(i);
        }
        sendData();
        setChanged();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("OutputItems", output.serializeNBT(registries));
        compound.putIntArray("Overflow",spoutputIndex);

        if (!clientPacket)
            return;
        compound.put("VisualizedItems", NBTHelper.writeCompoundList(visualizedOutputItems, ia -> (CompoundTag) ia.getValue().saveOptional(registries)));
        visualizedOutputItems.clear();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        output.deserializeNBT(registries, compound.getCompound("OutputItems"));

        int[] spoutput = compound.getIntArray("Overflow");
        spoutputIndex.clear();
        for(int index : spoutput) spoutputIndex.add(index);

        if (!clientPacket)
            return;

        NBTHelper.iterateCompoundList(compound.getList("VisualizedItems", Tag.TAG_COMPOUND),
                c -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, ItemStack.parseOptional(registries, c))));
    }


    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean hasTooltip = false;
        if(!validSpeed() && speed != 0) {
            RubberworksLang.addHint(tooltip, "hint.compressor.reverse");
            hasTooltip = true;
        }
        if(speed != 0 && getHeat() == BlazeBurnerBlock.HeatLevel.NONE){
            if(hasTooltip){RubberworksLang.text("").forGoggles(tooltip);}
            RubberworksLang.addHint(tooltip, "hint.compressor.heat");
            hasTooltip = true;
        }
        return hasTooltip;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean kinetic_tooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean fluid_tooltip = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());

        boolean item_tooltip = false;
        for (int i = 0; i < output.getSlots(); i++) {
            item_tooltip = true;
            ItemStack stackInSlot = output.getStackInSlot(i);
            if (stackInSlot.isEmpty())
                continue;
            RubberworksLang.text("")
                    .add(Component.translatable(stackInSlot.getDescriptionId())
                            .withStyle(ChatFormatting.GRAY))
                    .add(RubberworksLang.text(" x" + stackInSlot.getCount())
                            .style(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
        }

        return kinetic_tooltip || fluid_tooltip || item_tooltip;

    }




}
