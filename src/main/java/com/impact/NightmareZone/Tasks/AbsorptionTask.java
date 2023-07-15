package com.impact.NightmareZone.Tasks;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.InteractionApi.InventoryInteraction;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class AbsorptionTask extends Task
{
    private final Random r = new Random();

    int nextAbsorptionValue = r.nextInt(config.absorptionThresholdMax() - config.absorptionThresholdMin()) + config.absorptionThresholdMin();

    public AbsorptionTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //fail if:

        //not in the nightmare zone
        if (!Utils.isInNightmareZone(client))
            return false;

        //doesnt have absorptions
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return false;
        }

        if (Inventory.search()
                .idInList(Arrays.asList(ItemID.ABSORPTION_1, ItemID.ABSORPTION_2,
                        ItemID.ABSORPTION_3, ItemID.ABSORPTION_4))
                .empty())
            return false;

        //already met the absorption point threshold
        return client.getVar(Varbits.NMZ_ABSORPTION) < nextAbsorptionValue;
    }

    @Override
    public String getTaskDescription()
    {
        return "Drinking Absorptions";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return;
        }

        List<Widget> items = Inventory.search()
                .idInList(Arrays.asList(ItemID.ABSORPTION_1, ItemID.ABSORPTION_2,
                        ItemID.ABSORPTION_3, ItemID.ABSORPTION_4))
                .result();

        if (items.isEmpty())
        {
            return;
        }

        Widget item = items.get(0);

        if (item == null)
            return;

        InventoryInteraction.useItem(item, "Drink");

        nextAbsorptionValue = r.nextInt(config.absorptionThresholdMax() - config.absorptionThresholdMin()) + config.absorptionThresholdMin();
    }
}