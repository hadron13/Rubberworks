package io.github.hadron13.rubberworks.register;

import com.simibubi.create.foundation.data.*;
import com.tterrag.registrate.util.entry.BlockEntry;
import io.github.hadron13.rubberworks.Rubberworks;

import io.github.hadron13.rubberworks.blocks.compressor.CompressorBlock;
import io.github.hadron13.rubberworks.data.client.blockstates.*;
import io.github.hadron13.rubberworks.blocks.sapper.SapperBlock;
import io.github.hadron13.rubberworks.config.RubberworksStress;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class RubberworksBlocks {


    private static final CreateRegistrate REGISTRATE = Rubberworks.registrate().setCreativeTab(RubberworksCreativeModeTabs.MAIN_TAB);

    public static void register() {}


//    public static final BlockEntry<KilnBlock> KILN = REGISTRATE.block("kiln", KilnBlock::new)
//            .initialProperties(SharedProperties::stone)
//            .properties(p -> p  .mapColor(MapColor.METAL)
//                                .lightLevel(s -> s.getValue(KilnBlock.POWERED) ? 15 : 0))
//            .transform(pickaxeOnly())
//            .blockstate(new KilnGenerator()::generate)
//            .transform(RubberworksStress.setImpact(4.0))
//            .item()
//            .transform(customItemModel())
//            .register();
//
    public static final BlockEntry<SapperBlock> SAPPER = REGISTRATE.block("sapper", SapperBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .properties(p -> p.noOcclusion().mapColor(MapColor.METAL))
            //.addLayer(() -> RenderType::cutoutMipped)
            .blockstate(new PartialHorizontalBlockStateGen()::generate)
            .transform(RubberworksStress.setImpact(4.0))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<CompressorBlock> COMPRESSOR = REGISTRATE.block("compressor", CompressorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.METAL))
            .blockstate(new PartialHorizontalBlockStateGen().flipY(180)::generate)
            .transform(RubberworksStress.setImpact(4.0))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<Block> RUBBER_BLOCK =  REGISTRATE.block("rubber_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLACK).jumpFactor(1.5f).sound(SoundType.SLIME_BLOCK))
            .blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.standardModel(c, p)))
            .transform(pickaxeOnly())
            .lang("Rubber Block")
            .item()
            .build()
            .register();
}