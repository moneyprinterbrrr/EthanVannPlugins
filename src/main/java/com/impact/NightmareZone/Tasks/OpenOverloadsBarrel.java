package com.impact.NightmareZone.Tasks;

import java.util.Optional;

import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.TileObjectInteraction;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class OpenOverloadsBarrel extends Task
{
    public OpenOverloadsBarrel(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //fail if:

        //in the nightmare zone
        if (Utils.isInNightmareZone(client))
            return false;

        //check if dream is not created
        if (!Utils.isDreamCreated(client))
        {
            return false;
        }

        //if we have enough absorption doses in storage already
        if (client.getVarbitValue(3954) < config.absorptionDoses())
            return false;

        //if we have enough overload doses in storage already
        if (client.getVarbitValue(3953) < config.overloadDoses())
            return false;

        //already have overloads
        if (Utils.getOverloadDoseCount(client) >= config.overloadDoses())
            return false;

        Widget chatTitle = client.getWidget(WidgetInfo.CHATBOX_TITLE);

        if (chatTitle != null && !chatTitle.isHidden())
        {
            return !chatTitle.getText().contains("How many doses of overload potion will you withdraw?");
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Clicking Overload Barrel";
    }

    @Override
    public void onGameTick(GameTick event)
    {

        Optional<TileObject> gameObjects = TileObjects.search().withId(ObjectID.OVERLOAD_POTION).first();

        if (gameObjects == null || gameObjects.isEmpty())
        {
            return;
        }

        TileObject gameObject = gameObjects.get();

        if (gameObject == null)
            return;

        TileObjectInteraction.interact(gameObject, "Take");
    }
}