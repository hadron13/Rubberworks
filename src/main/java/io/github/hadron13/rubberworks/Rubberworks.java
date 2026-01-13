package io.github.hadron13.rubberworks;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import io.github.hadron13.rubberworks.config.RubberworksConfig;
import io.github.hadron13.rubberworks.data.RubberworksDatagen;
import io.github.hadron13.rubberworks.register.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rubberworks.MODID)
public class Rubberworks {

    public static final String MODID = "rubberworks";
    public static final String DISPLAY_NAME = "Rubberworks";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static IEventBus modEventBus;
    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    static {
        REGISTRATE
                .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
                .setTooltipModifierFactory((item) -> (new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)).andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }


    public Rubberworks(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();


        REGISTRATE.registerEventListeners(modEventBus);

        RubberworksCreativeModeTabs.register(modEventBus);
        RubberworksBlocks.register();
        RubberworksItems.register();
        RubberworksBlockEntities.register();
        RubberworksFluids.register();
        RubberworksPartialModels.init();
        RubberworksRecipeTypes.register(modEventBus);
        RubberworksConfig.register(modLoadingContext, modContainer);

        modEventBus.addListener(EventPriority.HIGHEST, RubberworksDatagen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, RubberworksDatagen::gatherData);

    }





    public static CreateRegistrate registrate(){
        return REGISTRATE;
    }


    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

