package io.github.hadron13.rubberworks.blocks.sapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import io.github.hadron13.rubberworks.register.RubberworksPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.minecraft.core.Direction.*;
import static net.minecraft.core.Direction.SOUTH;

public class SapperRenderer extends KineticBlockEntityRenderer<SapperBlockEntity> {

    public SapperRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SapperBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getLevel())) return;

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(HORIZONTAL_FACING);

        VertexConsumer vb = buffer.getBuffer(RenderType.solid());

//        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState);
//        standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, vb);

        SuperByteBuffer cogModel = CachedBuffers.partialDirectional(
                AllPartialModels.SHAFTLESS_COGWHEEL, blockState, facing, () -> {
            PoseStack poseStack = new PoseStack();
                    TransformStack.of(poseStack)
                    .center()
                    .rotateToFace(facing)
                    .rotate(Axis.XN.rotationDegrees(90))
                    .uncenter();
            return poseStack;
        });

        standardKineticRotationTransform(cogModel, be, light).renderInto(ms, vb);

        float renderedHeadOffset = be.getRenderedHeadOffset(partialTicks);
        float speed = be.getRenderedHeadRotationSpeed(partialTicks);
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float angle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;

        int x_multiplier = (facing==WEST)?  1 : (facing==EAST)?  -1 : 0;
        int z_multiplier = (facing==NORTH)? 1 : (facing==SOUTH)? -1 : 0;

        SuperByteBuffer poleRender = CachedBuffers.partialDirectional(RubberworksPartialModels.SAPPER_POLE, blockState, facing, () -> {
            PoseStack poseStack = new PoseStack();
            TransformStack.of(poseStack)
                    .center()
                    .rotateToFace(facing)
                    .uncenter();
            return poseStack;
        });
        poleRender.translate(renderedHeadOffset * x_multiplier ,  0 , renderedHeadOffset * z_multiplier)
                .light(light)
                .renderInto(ms, vb);

        VertexConsumer vbCutout = buffer.getBuffer(RenderType.cutoutMipped());
        SuperByteBuffer headRender = CachedBuffers.partialDirectional(RubberworksPartialModels.SAPPER_HEAD, blockState, facing, () -> {
            PoseStack poseStack = new PoseStack();
            TransformStack.of(poseStack)
                    .center()
                    .rotateToFace(facing)
                    .rotate(Axis.XN.rotationDegrees(90))
                    .uncenter();
            return poseStack;
        });
        headRender.rotateCentered(angle, facing)
                .translate(renderedHeadOffset * x_multiplier ,  0 , renderedHeadOffset * z_multiplier)
                .light(light)
                .renderInto(ms, vbCutout);

    }

}