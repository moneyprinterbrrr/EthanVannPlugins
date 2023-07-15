package com.impact.NightmareZone.Tasks;

import java.util.Optional;

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

public class RockCakeTask extends Task
{
    public RockCakeTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
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

        //not overloaded
        if (client.getVar(Varbits.NMZ_OVERLOAD_REFRESHES_REMAINING) == 0)
            return false;

        //don't have rock cake
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return false;
        }

        if (Inventory.search()
                .withId(ItemID.DWARVEN_ROCK_CAKE_7510).first().isEmpty())
            return false;

        //already 1 hp
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= 1)
            return false;

        //out of absorption points
        return client.getVar(Varbits.NMZ_ABSORPTION) > 0;
    }

    @Override
    public String getTaskDescription()
    {
        return "Rock caking";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        if (NightmareZonePlugin.rockCakeDelay > 0)
        {
            NightmareZonePlugin.rockCakeDelay--;
            return;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return;
        }

        Optional<Widget> items = Inventory.search()
                .withId(ItemID.DWARVEN_ROCK_CAKE_7510).first();

        if (items == null || items.isEmpty())
        {
            return;
        }

        Widget item = items.get();

        InventoryInteraction.useItem(item, "Guzzle");
    }
}