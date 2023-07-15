package com.impact.NightmareZone.Tasks;

import java.util.Optional;

import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.TileObjectInteraction;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class DrinkPotionTask extends Task
{
    public DrinkPotionTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        // Drinks potion to start dream
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //in nmz
        if (Utils.isInNightmareZone(client))
            return false;

        //doesn't have absorptions
        if (Utils.getAbsorptionDoseCount(client) < config.absorptionDoses())
            return false;

        //doesn't have overloads
        if (Utils.getOverloadDoseCount(client) < config.overloadDoses())
            return false;

        //dream isn't created
        if (!Utils.isDreamCreated(client))
        {
            return false;
        }

        Widget acceptWidget = client.getWidget(129, 6);

        return acceptWidget == null || acceptWidget.isHidden();
    }

    @Override
    public String getTaskDescription()
    {
        return "Drink Potion";
    }

    @Override
    public void onGameTick(GameTick event)
    {
		/*
		Option:	Drink
		Target:	<col=ffff><col=ff9040>Potion
		Identifier:	26291 //game object id
		Opcode:	MenuOpcode.GAME_OBJECT_FIRST_OPTION
		Param0:	45 //scene min x
		Param1:	53 //scene min y
		 */

        Optional<TileObject> results = TileObjects.search().withId(26291).first();

        if (results == null || results.isEmpty())
            return;

        TileObject object = results.get();

        if (object == null)
            return;

        TileObjectInteraction.interact(object, "Drink");
    }
}