package io.github.hadron13.rubberworks.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import io.github.hadron13.rubberworks.register.RubberworksBlocks;
import io.github.hadron13.rubberworks.register.RubberworksPartialModels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Block;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.minecraft.core.Direction.WEST;

public class AnimatedSapper extends AnimatedKinetics {

    public Block log;
    public Block leaf;
    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        final float blockSize = 1f;
        final float pixelSize = blockSize/16f;

        blockElement(cogwheel())
                .rotateBlock(0, getCurrentAngle() * 2, 90)
                .atLocal(0, blockSize * 3, 0)
                .scale(scale)
                .render(graphics);

        blockElement(RubberworksBlocks.SAPPER.getDefaultState().setValue(HORIZONTAL_FACING, WEST))
                .atLocal(0, blockSize * 3, 0)
                .scale(scale)
                .render(graphics);

        blockElement(RubberworksPartialModels.SAPPER_HEAD)
                .rotateBlock(0, getCurrentAngle() * -4,90 )
                .atLocal(blockSize + pixelSize * 4, blockSize * 3, 0)
                .scale(scale)
                .render(graphics);

        blockElement(RubberworksPartialModels.SAPPER_POLE)
                .rotateBlock(0, 90,0 )
                .atLocal(blockSize + pixelSize * 4, blockSize * 3, 0)
                .scale(scale)
                .render(graphics);

        // render da arvrinha
        for(int y = 1; y <= 3; y++) {
            blockElement(log.defaultBlockState())
                    .atLocal(2 * blockSize, blockSize * y, 0)
                    .scale(scale)
                    .render(graphics);
        }

        int[] leafX = {2, 2, 1, 3};
        int[] leafZ = {-1, 1, 0, 0};

        for(int i = 0; i < 4; i++) {
            blockElement(leaf.defaultBlockState())
                    .atLocal(leafX[i] * blockSize, blockSize, leafZ[i] * blockSize)
                    .scale(scale)
                    .render(graphics);
        }
        blockElement(leaf.defaultBlockState())
                .atLocal(blockSize * 2, 0, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}