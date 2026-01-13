package io.github.hadron13.rubberworks.blocks.compressor;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.github.hadron13.rubberworks.register.RubberworksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CompressorBlock extends HorizontalKineticBlock implements IBE<CompressorBlockEntity> {

    public CompressorBlock(Properties properties){
        super(properties);
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState state){
        return state.getValue(HORIZONTAL_FACING).getClockWise()
                .getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }
    public static boolean hasPipeTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING).getOpposite() == face;
    }

    @Override
    public Class<CompressorBlockEntity> getBlockEntityClass(){
        return CompressorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends CompressorBlockEntity> getBlockEntityType(){
        return RubberworksBlockEntities.COMPRESSOR.get();
    }





}
