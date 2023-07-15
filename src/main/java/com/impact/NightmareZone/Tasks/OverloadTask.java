package com.impact.NightmareZone.Tasks;

import java.util.Arrays;
import java.util.List;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.InteractionApi.InventoryInteraction;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class OverloadTask extends Task
{
    public OverloadTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
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

        //already overloaded
        if (client.getVar(Varbits.NMZ_OVERLOAD_REFRESHES_REMAINING) != 0)
            return false;

        //don't have overloads
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return false;
        }

        if (Inventory.search()
                .idInList(Arrays.asList(ItemID.OVERLOAD_1, ItemID.OVERLOAD_2,
                        ItemID.OVERLOAD_3, ItemID.OVERLOAD_4))
                .empty())
            return false;

        //less than 50 hp
        return client.getBoostedSkillLevel(Skill.HITPOINTS) > 50;
    }

    @Override
    public String getTaskDescription()
    {
        return "Drinking Overload";
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
                .idInList(Arrays.asList(ItemID.OVERLOAD_1, ItemID.OVERLOAD_2,
                        ItemID.OVERLOAD_3, ItemID.OVERLOAD_4))
                .result();

        if (items == null || items.isEmpty())
        {
            return;
        }

        Widget item = items.get(0);

        if (item == null)
            return;

        InventoryInteraction.useItem(item, "Drink");
    }
}