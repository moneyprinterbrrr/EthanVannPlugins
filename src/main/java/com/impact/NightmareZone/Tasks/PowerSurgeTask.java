package com.impact.NightmareZone.Tasks;

import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.TileObjectInteraction;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

import java.util.Optional;

public class PowerSurgeTask extends Task
{
    public PowerSurgeTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        if (!config.powerSurge())
            return false;

        //in the nightmare zone
        if (!Utils.isInNightmareZone(client))
            return false;

        Optional<TileObject> results = TileObjects.search().withId(ObjectID.POWER_SURGE).first();

        if (results == null || results.isEmpty())
        {
            return false;
        }

        TileObject obj = results.get();

        if (obj == null)
        {
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Power Surge";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Optional<TileObject> results = TileObjects.search().withId(ObjectID.POWER_SURGE).first();

        if (results == null || results.isEmpty())
        {
            return;
        }

        TileObject obj = results.get();

        if (obj == null)
        {
            return;
        }

        TileObjectInteraction.interact(obj, "Activate");
    }
}