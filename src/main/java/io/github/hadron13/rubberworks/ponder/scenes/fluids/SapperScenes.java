package io.github.hadron13.rubberworks.ponder.scenes.fluids;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.github.hadron13.rubberworks.blocks.sapper.SapperBlockEntity;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SapperScenes {
    public static void sapper(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("sapper", "Using a sapper to gather resources");
        scene.configureBasePlate(0, 0, 7);

        Selection power = util.select().fromTo(4, 1, 4, 5, 1, 7)
                .add(util.select().position(6, 0 ,7));

        Selection tree = util.select().fromTo(2, 1, 3, 2, 4, 3)
                .add(util.select().fromTo(0, 3, 1, 4, 7, 6));

        BlockPos sapper = util.grid().at(4, 1, 3);
        BlockPos pump = sapper.east(2);

        Selection pipes = util.select().fromTo(5, 1, 3, 7, 1, 3)
                .add(util.select().position(6,1,4))
                .add(util.select().position(7, 0, 3));

        scene.world().showSection(util.select().fromTo(0, 0, 0, 6, 0, 6), Direction.UP);
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().position(sapper), 0f);
        scene.world().setKineticSpeed(util.select().position(pump), 0f);

        scene.world().showSection(util.select().position(sapper), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Sappers are machines used to sap fluids from trees")
                .pointAt(util.vector().topOf(sapper));

        scene.idle(70);

        scene.world().showSection(power, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(sapper), -32f);

        scene.overlay().showText(45)
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can power them with cogs")
                .pointAt(util.vector().topOf(sapper.south()));

        scene.idle(50);

        scene.world().showSection(tree, Direction.DOWN);

        scene.idle(20);

        scene.world().showSection(pipes, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(pump), 32f);
        scene.world().modifyBlockEntity(sapper, SapperBlockEntity.class, be -> {
            be.ponderFill(300);
        });

        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .text("And then pull the output fluids from the back")
                .pointAt(util.vector().topOf(sapper));

        scene.world().propagatePipeChange(util.grid().at(6, 1, 3));

        scene.idle(30);
    }
}
