package io.github.hadron13.rubberworks.register;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import io.github.hadron13.rubberworks.Rubberworks;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class RubberworksCreativeModeTabs {


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rubberworks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rubberworks.main"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(RubberworksBlocks.COMPRESSOR::asStack)
                    .displayItems(new RegistrateDisplayItemsGenerator(RubberworksCreativeModeTabs.MAIN_TAB))
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }


    private static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {


        private final DeferredHolder<CreativeModeTab, CreativeModeTab> tabFilter;

        public RegistrateDisplayItemsGenerator(DeferredHolder<CreativeModeTab, CreativeModeTab> tabFilter) {

            this.tabFilter = tabFilter;
        }

        private List<Item> collectBlocks() {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Block, Block> entry : Rubberworks.registrate().getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tabFilter))
                    continue;
                Item item = entry.get()
                        .asItem();
                if (item == Items.AIR)
                    continue;
                items.add(item);
            }
            items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
            return items;

        }

        private List<Item> collectItems(DeferredHolder<CreativeModeTab, CreativeModeTab> tab, Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();


            for (RegistryEntry<Item, Item> entry : Rubberworks.registrate().getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }
        List<Item> exclude = List.of(
        );
        private static void outputAll(CreativeModeTab.Output output, List<Item> items) {
            for (Item item : items) {
                output.accept(item);
            }
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
            List<Item> items = new LinkedList<>();
            items.addAll(collectBlocks());

            items.addAll(collectItems(tabFilter, (item) -> exclude.contains(item) || item instanceof SequencedAssemblyItem));

            outputAll(output, items);
        }

    }
}
