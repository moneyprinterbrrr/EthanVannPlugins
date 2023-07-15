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

public class SearchRewardsChestTask extends Task
{
    public SearchRewardsChestTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //in the nightmare zone
        if (Utils.isInNightmareZone(client))
            return false;

        //if we have enough absorption doses in storage already
        if (client.getVarbitValue(3954) >= config.absorptionDoses() &&
                client.getVarbitValue(3953) >= config.overloadDoses())
            return false;

        //has absorptions && has overloads
        if (Utils.getAbsorptionDoseCount(client) >= config.absorptionDoses() &&
                Utils.getOverloadDoseCount(client) >= config.overloadDoses())
            return false;

        //get the game object
        Optional<TileObject> results = TileObjects.search().withId(ObjectID.REWARDS_CHEST).first();

        if (results == null || results.isEmpty())
        {
            return false;
        }

        TileObject obj = results.get();

        if (obj == null)
        {
            return false;
        }

        Widget rewardsShopWidget = client.getWidget(206, 0);

        if (rewardsShopWidget != null && !rewardsShopWidget.isHidden())
        {
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Search Rewards Chest";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Optional<TileObject> results = TileObjects.search().withId(ObjectID.REWARDS_CHEST).first();

        if (results == null || results.isEmpty())
        {
            return;
        }

        TileObject obj = results.get();

        if (obj == null)
        {
            return;
        }

        TileObjectInteraction.interact(obj, "Search");
    }
}