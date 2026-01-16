package io.github.hadron13.rubberworks.ponder.scenes.fluids;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.github.hadron13.rubberworks.blocks.compressor.CompressorBlockEntity;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CompressorScenes {
    public static void compressor(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("compressor", "Using a compressor to process fluids");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);

        BlockPos compressor = util.grid().at(2, 2, 2);
        BlockPos depot      = util.grid().at(1, 1, 2);
        Selection large_cog  = util.select().position(1, 0, 5);
        Selection intermediate_cogs = util.select().fromTo(2, 1, 3, 2, 1, 5);
        Selection last_cog = util.select().position(2, 2, 3);
        BlockPos pump = util.grid().at(3, 1, 3);
        Selection tank = util.select().fromTo(3, 1, 4, 3, 2, 4);
        Selection pipes = util.select().fromTo(3, 1, 3, 3, 2, 2);

        scene.idle(10);
        scene.overlay().showText(40)
                .placeNearTarget()
                .attachKeyFrame()
                .text("The Compressor can process fluids into items")
                .pointAt(Vec3.atCenterOf(compressor));

        scene.idle(60);

        scene.rotateCameraY(-90f);
        scene.idle(20);
        scene.overlay().showText(40)
                .placeNearTarget()
                .attachKeyFrame()
                .text("It can be powered with a shaft from the side")
                .pointAt(Vec3.atCenterOf(compressor));
        scene.idle(30);
        scene.world().setKineticSpeed(large_cog, -16f);
        scene.world().setKineticSpeed(intermediate_cogs, 32f);
        scene.world().showSection(large_cog, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(intermediate_cogs, Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(last_cog.add(util.select().position(compressor)), -32f);
        scene.world().showSection(last_cog, Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .text("It must move clockwise from the right side")
                .pointAt(Vec3.atCenterOf(compressor));
        scene.idle(80);

        scene.rotateCameraY(180f);
        scene.idle(10);
        scene.world().showSection(tank, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(pipes, -32f);
        scene.world().showSection(pipes, Direction.DOWN);

        FluidStack content = new FluidStack(Fluids.LAVA
                .getSource(), 16000);
        scene.world().modifyBlockEntity(util.grid().at(3, 1, 4), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content, IFluidHandler.FluidAction.EXECUTE));
        scene.world().propagatePipeChange(pump);

        scene.overlay().showText(40)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Fluids can be inserted from the back")
                .pointAt(Vec3.atCenterOf(compressor));
        scene.idle(60);

        scene.rotateCameraY(-90f);
        scene.idle(10);

        scene.world().replaceBlocks(util.select().position(compressor.below()), Blocks.CAMPFIRE.defaultBlockState(), true);

        scene.overlay().showText(40)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Minimum heat must be provided")
                .pointAt(Vec3.atCenterOf(compressor.below()));
        scene.idle(60);

        scene.world().showSection(util.select().position(1, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntityNBT(util.select().position(compressor), CompressorBlockEntity.class, nbt -> {
            nbt.put("VisualizedItems",
                    NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, new ItemStack(Blocks.OBSIDIAN))),
                            ia -> ia.getValue()
                                    .serializeNBT()));
        });
        scene.world().createItemOnBeltLike(compressor.below().west(), Direction.UP, new ItemStack(Items.OBSIDIAN));
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(compressor.below().west()), Pointing.DOWN, 30).withItem(new ItemStack(Items.OBSIDIAN));

        scene.overlay().showText(40)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Lastly, the output can be retrieved with a depot, belt or basin")
                .pointAt(Vec3.atCenterOf(compressor.below().west()));
    }
}
