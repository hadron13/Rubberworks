package io.github.hadron13.rubberworks.register;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.hadron13.rubberworks.Rubberworks;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class RubberworksPartialModels {
    public static final PartialModel
            SAPPER_HEAD = block("sapper/head"),
            SAPPER_POLE = block("sapper/pole"),
            COMPRESSOR_ROLL = block("compressor/roll");
    ;

    public static final Map<FluidTransportBehaviour.AttachmentTypes.ComponentPartials, Map<Direction, PartialModel>> STEEL_PIPE_ATTACHMENTS =
            new EnumMap<>(FluidTransportBehaviour.AttachmentTypes.ComponentPartials.class);

    static {
        for (FluidTransportBehaviour.AttachmentTypes.ComponentPartials type : FluidTransportBehaviour.AttachmentTypes.ComponentPartials
                .values()) {
            Map<Direction, PartialModel> map = new HashMap<>();
            for (Direction d : Iterate.directions) {
                String asId = Lang.asId(type.name());
                map.put(d, block("steel_fluid_pipe/" + asId + "/" + Lang.asId(d.getSerializedName())));
            }
            STEEL_PIPE_ATTACHMENTS.put(type, map);
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Rubberworks.asResource("block/" + path));
    }

    private static PartialModel item(String path) {
        return PartialModel.of(Rubberworks.asResource("item/" + path));
    }

    private static PartialModel entity(String path) {
        return PartialModel.of(Rubberworks.asResource("entity/" + path));
    }

    public static void init() {
        // init static fields
    }

}
