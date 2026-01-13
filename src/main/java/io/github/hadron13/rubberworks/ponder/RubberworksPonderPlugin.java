package io.github.hadron13.rubberworks.ponder;

import io.github.hadron13.rubberworks.Rubberworks;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.*;
import net.minecraft.resources.ResourceLocation;

public class RubberworksPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Rubberworks.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        RubberworksPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        RubberworksPonderTags.register(helper);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {

    }

    @Override
    public void onPonderLevelRestore(PonderLevel ponderLevel) {

    }

    @Override
    public void indexExclusions(IndexExclusionHelper helper) {

    }
}
