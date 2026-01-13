package io.github.hadron13.rubberworks.register;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.hadron13.rubberworks.Rubberworks;
import net.minecraft.world.item.Item;

public class RubberworksItems {
    private static final CreateRegistrate REGISTRATE = Rubberworks.registrate();

    public static void register() {}
    public static final ItemEntry<Item> RUBBER = ingredient("rubber"),
            RUBBER_SHEET = ingredient("rubber_sheet");

    private static ItemEntry<Item> ingredient(String name) {
        return REGISTRATE.item(name, Item::new)
                .register();
    }

}
