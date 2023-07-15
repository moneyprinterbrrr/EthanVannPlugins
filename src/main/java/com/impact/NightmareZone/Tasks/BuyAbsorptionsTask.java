package com.impact.NightmareZone.Tasks;

import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class BuyAbsorptionsTask extends Task
{
    public BuyAbsorptionsTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
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
        if (client.getVarbitValue(3954) >= config.absorptionDoses())
            return false;

        //has absorptions && has overloads
        if (Utils.getAbsorptionDoseCount(client) >= config.absorptionDoses() &&
                Utils.getOverloadDoseCount(client) >= config.overloadDoses())
            return false;

        Widget rewardsShopWidget = client.getWidget(206, 0);

        if (rewardsShopWidget == null || rewardsShopWidget.isHidden())
        {
            return false;
        }

        try
        {
            Widget benefitsPanel = client.getWidget(206, 6);

            if (benefitsPanel == null || benefitsPanel.isHidden())
            {
                return false;
            }

            Widget absorptionWidget = benefitsPanel.getChild(9);

            if (absorptionWidget == null || absorptionWidget.isHidden() || !absorptionWidget.getName().equals("<col=ff9040>Absorption (1)"))
            {
                return false;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            //absorption potion doesnt exist? lets dip...
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Buy Absorptions";
    }

    @Override
    public void onGameTick(GameTick event)
    {
		/*
		Option:	Buy-50
		Target:	<col=ff9040>Absorption (1)
		Identifier:	4
		Opcode:	57
		Param0:	9
		Param1:	13500422
		*/

        //if amount of points is less than doses to buy * 1000
        if (client.getVar(VarPlayer.NMZ_REWARD_POINTS) / 1000 < (config.absorptionDoses() - client.getVarbitValue(3954)))
        {
            plugin.stopPlugin("Not enough points to buy absorption potions!");
            return;
        }

        //client.getWidget(206 6)[9]
        Widget rewardsShopWidget = client.getWidget(206, 6);

        if (rewardsShopWidget == null || rewardsShopWidget.isHidden())
        {
            return;
        }

        Widget absorptionWidget;
        try
        {
            absorptionWidget = rewardsShopWidget.getChild(9);
        }
        catch (IndexOutOfBoundsException e)
        {
            //absorption potion doesnt exist? lets dip...
            return;
        }

        if (absorptionWidget == null || absorptionWidget.isHidden() || !absorptionWidget.getName().equals("<col=ff9040>Absorption (1)"))
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(absorptionWidget, "Buy-50");
    }
}