package com.impact.NightmareZone.Tasks;

import java.util.Optional;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.NPCInteraction;
import com.example.PacketUtils.WidgetInfoExtended;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class DominicDreamTask extends Task
{
    public DominicDreamTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //fail if:

        //check if dream is created
        if (Utils.isDreamCreated(client))
        {
            return false;
        }

        //in the nightmare zone
        if (Utils.isInNightmareZone(client))
            return false;

		/*debugMessage.removeLast();
		debugMessage.addLast(getClass().getName() + ":\thas absorptions");

		//has absorptions
		if (Utils.getAbsorptionDoseCount(client) >= config.absorptionDoses())
			return false;

		debugMessage.removeLast();
		debugMessage.addLast(getClass().getName() + ":\thas overloads");

		//has overloads
		if (Utils.getOverloadDoseCount(client) >= config.overloadDoses())
			return false;*/


        //DIALOG_OPTION_OPTION1[0] == Which dream would you like to experience?
        Widget dialogOption1Widget = client.getWidget(WidgetInfoExtended.DIALOG_OPTION_OPTION1.getId());

        if (dialogOption1Widget != null && !dialogOption1Widget.isHidden())
        {
            return false;
        }

        Widget dialogNpcContinueWidget = client.getWidget(WidgetInfoExtended.DIALOG_NPC_CONTINUE.getId());

        if (dialogNpcContinueWidget != null && !dialogNpcContinueWidget.isHidden())
        {
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Clicking on Dominic";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Optional<NPC> result = NPCs.search().withId(NpcID.DOMINIC_ONION).first();

        if (result == null || result.isEmpty())
        {
            return;
        }

        NPC dominicOnion = result.get();

        if (dominicOnion == null)
            return;

        NPCInteraction.interact(dominicOnion, "Dream");
    }
}