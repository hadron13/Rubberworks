package io.github.hadron13.rubberworks.register;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.blocks.compressor.CompressorBlockEntity;
import io.github.hadron13.rubberworks.blocks.compressor.CompressorVisual;
import io.github.hadron13.rubberworks.blocks.compressor.CompressorRenderer;
import io.github.hadron13.rubberworks.blocks.sapper.SapperBlockEntity;
import io.github.hadron13.rubberworks.blocks.sapper.SapperVisual;
import io.github.hadron13.rubberworks.blocks.sapper.SapperRenderer;

public class RubberworksBlockEntities {

    private static final CreateRegistrate REGISTRATE = Rubberworks.registrate();

    public static final BlockEntityEntry<SapperBlockEntity> SAPPER = REGISTRATE
            .blockEntity("sapper", SapperBlockEntity::new)
            .visual(() -> SapperVisual::new)
            .validBlocks(RubberworksBlocks.SAPPER)
            .renderer(() -> SapperRenderer::new)
            .register();

    public static final BlockEntityEntry<CompressorBlockEntity> COMPRESSOR = REGISTRATE
            .blockEntity("compressor", CompressorBlockEntity::new)
            .visual(() -> CompressorVisual::new)
            .validBlocks(RubberworksBlocks.COMPRESSOR)
            .renderer(() -> CompressorRenderer::new)
            .register();

    public static void register() {}
}