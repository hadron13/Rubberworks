package io.github.hadron13.rubberworks.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import io.github.hadron13.rubberworks.ponder.scenes.fluids.CompressorScenes;
import io.github.hadron13.rubberworks.ponder.scenes.fluids.SapperScenes;
import io.github.hadron13.rubberworks.register.RubberworksBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import static com.simibubi.create.infrastructure.ponder.AllCreatePonderTags.FLUIDS;
import static com.simibubi.create.infrastructure.ponder.AllCreatePonderTags.KINETIC_APPLIANCES;

public class RubberworksPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(RubberworksBlocks.COMPRESSOR)
                .addStoryBoard("compressor", CompressorScenes::compressor, KINETIC_APPLIANCES, FLUIDS);

        HELPER.forComponents(RubberworksBlocks.SAPPER)
                .addStoryBoard("sapper", SapperScenes::sapper, KINETIC_APPLIANCES, FLUIDS);
    }

}
