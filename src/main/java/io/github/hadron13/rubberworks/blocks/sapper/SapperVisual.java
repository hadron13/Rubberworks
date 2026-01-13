package io.github.hadron13.rubberworks.blocks.sapper;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import io.github.hadron13.rubberworks.register.RubberworksPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.minecraft.core.Direction.*;

public class SapperVisual extends SingleAxisRotatingVisual<SapperBlockEntity> implements SimpleDynamicVisual {

    private final RotatingInstance drillHead;
    private final OrientedInstance drillPole;
    private final SapperBlockEntity sapper;


    final Direction direction;


    public SapperVisual(VisualizationContext context, SapperBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL));
        this.sapper = blockEntity;

        direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        drillHead = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(RubberworksPartialModels.SAPPER_HEAD)).createInstance();

        drillHead.setRotationAxis(direction.getAxis());

        drillPole = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(RubberworksPartialModels.SAPPER_POLE)).createInstance();

        drillHead.rotateToFace(direction);
        drillPole.rotateToFace(direction);

        animate(partialTick);
    }

    private void animate(float pt) {
        float renderedHeadOffset = sapper.getRenderedHeadOffset(pt);

        transformHead(renderedHeadOffset, pt);
        transformPole(renderedHeadOffset);
    }

    private void transformHead(float renderedHeadOffset, float pt) {
        float speed = sapper.getRenderedHeadRotationSpeed(pt);

        int x_multiplier = (direction==WEST)?  1 : (direction==EAST)?  -1 : 0;
        int z_multiplier = (direction==NORTH)? 1 : (direction==SOUTH)? -1 : 0;


        drillHead.setPosition(getVisualPosition())
                //.rotateToFace(direction)
                .nudge(renderedHeadOffset * x_multiplier ,  0 , renderedHeadOffset * z_multiplier)
                .setRotationalSpeed(speed * 2 * RotatingInstance.SPEED_MULTIPLIER)
                .setChanged();
    }

    private void transformPole(float renderedHeadOffset) {
        int x_multiplier = (direction==WEST)?  1 : (direction==EAST)?  -1 : 0;
        int z_multiplier = (direction==NORTH)? 1 : (direction==SOUTH)? -1 : 0;
        drillPole.position(getVisualPosition())
                //.rotateToFace(direction)
                .translatePosition(renderedHeadOffset * x_multiplier ,  0 , renderedHeadOffset * z_multiplier)
                .setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    @Override
    public void updateLight(float partialTicks) {
        super.updateLight(partialTicks);
        relight(pos.relative(blockState.getValue(HORIZONTAL_FACING).getOpposite(), 1), drillHead);
        relight(pos, drillPole);
    }

    @Override
    public void _delete() {
        super._delete();
        drillHead.delete();
        drillPole.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(drillHead);
        consumer.accept(drillPole);
    }
}