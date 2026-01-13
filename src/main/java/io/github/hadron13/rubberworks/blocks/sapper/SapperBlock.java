package io.github.hadron13.rubberworks.blocks.sapper;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import io.github.hadron13.rubberworks.register.RubberworksBlockEntities;
import io.github.hadron13.rubberworks.register.RubberworksShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SapperBlock extends HorizontalKineticBlock
    implements IBE<SapperBlockEntity>, ICogWheel {

  public SapperBlock(Properties properties) { super(properties); }

  @Override
  public Axis getRotationAxis(BlockState state) {
    return state.getValue(HORIZONTAL_FACING).getAxis();
  }
  @Override
  public boolean canSurvive(BlockState state, LevelReader worldIn,
                            BlockPos pos) {
    BlockPos extensionPosition =
        pos.relative(state.getValue(HORIZONTAL_FACING).getOpposite());
    return worldIn.getBlockState(extensionPosition).isAir();
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn,
                             BlockPos pos, CollisionContext context) {
    return RubberworksShapes.SAPPER.get(state.getValue(HORIZONTAL_FACING));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {

    BlockPos pos = context.getClickedPos();
    Level world = context.getLevel();
    Direction dir = context.getHorizontalDirection().getOpposite();

    BlockState finalState =
        defaultBlockState().setValue(HORIZONTAL_FACING, dir);

    for (int i = 0; i < 4; i++) {
      if (hasLogInDirection(dir, pos, world))
        return finalState.setValue(HORIZONTAL_FACING, dir.getOpposite());
      dir = dir.getClockWise();
    }

    for (int i = 0; i < 4; i++) {
      if (hasPipeInDirection(dir, pos, world))
        return finalState.setValue(HORIZONTAL_FACING, dir);
      dir = dir.getClockWise();
    }

    return finalState;
  }

  public static boolean hasLogInDirection(Direction direction, BlockPos pos,
                                          Level world) {
    return world.getBlockState(pos.relative(direction, 2)).is(BlockTags.LOGS);
  }
  public static boolean hasPipeInDirection(Direction dir, BlockPos pos,
                                           Level world) {
    BlockPos neighborPos = pos.relative(dir);
    return FluidPipeBlock.canConnectTo(world, neighborPos,
                                       world.getBlockState(neighborPos), dir);
  }
  //    public static boolean

  public static boolean hasPipeTowards(LevelReader world, BlockPos pos,
                                       BlockState state, Direction face) {
    return state.getValue(HORIZONTAL_FACING) == face;
  }

  @Override
  public Class<SapperBlockEntity> getBlockEntityClass() {
    return SapperBlockEntity.class;
  }
  @Override
  public BlockEntityType<? extends SapperBlockEntity> getBlockEntityType() {
    return RubberworksBlockEntities.SAPPER.get();
  }

}
