package io.github.hadron13.rubberworks;

import io.github.hadron13.rubberworks.blocks.compressor.CompressorBlockEntity;
import io.github.hadron13.rubberworks.blocks.sapper.SapperBlockEntity;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber
public class RubberworksCommonEvents {
    @net.neoforged.bus.api.SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CompressorBlockEntity.registerCapabilities(event);
        SapperBlockEntity.registerCapabilities(event);
    }
}
