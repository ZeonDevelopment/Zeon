package dextro.zeon.addon.modules.Misc;

import java.util.ArrayList;
import java.util.List;

import dextro.zeon.addon.Zeon;
import dextro.zeon.addon.utils.Ezz;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;

public class ItemShrader extends Module {

    public ItemShrader() {
        super(Zeon.Misc, "item-shrader", "Automaticly removes choosing items.");
    }


    private final SettingGroup g = settings.getDefaultGroup();
    private final Setting<List<Item>> items = g.add(new ItemListSetting.Builder()
        .name("items")
        .defaultValue(new ArrayList<>(0))
        .build()
    );


    private boolean isInventory() {
        return (mc.player.currentScreenHandler != null && mc.player.currentScreenHandler.syncId == 0);
    }

    @EventHandler
    private void a(TickEvent.Pre e) {
              if(items.get().size() > 5)
              {
            	  error("You cant use items count > 5 in free version! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
                  toggle();
                  return;
              }

        if (isInventory()) {
            FindItemResult item = InvUtils.find(a -> items.get().contains(a.getItem()));
            if (item.found()) mc.interactionManager.clickSlot(0, Ezz.invIndexToSlotId(item.getSlot()), 300, SlotActionType.SWAP, mc.player);
        }

    }
}