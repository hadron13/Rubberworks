package io.github.hadron13.rubberworks.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.register.RubberworksBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import static com.simibubi.create.infrastructure.ponder.AllCreatePonderTags.FLUIDS;
import static com.simibubi.create.infrastructure.ponder.AllCreatePonderTags.KINETIC_APPLIANCES;

public class RubberworksPonderTags {

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addToTag(FLUIDS)
                .add(RubberworksBlocks.SAPPER)
                .add(RubberworksBlocks.COMPRESSOR);

    }
}
