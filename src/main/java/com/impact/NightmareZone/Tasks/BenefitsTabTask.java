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

public class BenefitsTabTask extends Task
{
    public BenefitsTabTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
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
                client.getVarbitValue(3953) >= config.overloadDoses()) {
            return false;
        }

        //has absorptions && has overloads
        if (Utils.getAbsorptionDoseCount(client) >= config.absorptionDoses() &&
                Utils.getOverloadDoseCount(client) >= config.overloadDoses())
            return false;

        Widget rewardsShopWidget = client.getWidget(206, 0);

        if (rewardsShopWidget == null || rewardsShopWidget.isHidden())
        {
            return false;
        }

        Widget rewardsTabList = client.getWidget(206, 2);

        if (rewardsTabList == null || rewardsTabList.isHidden())
        {
            return false;
        }

        try
        {
            Widget benefitTab = rewardsTabList.getChild(5);

            if (benefitTab == null || !benefitTab.getText().equals("Benefits"))
            {
                return false;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            //child not found somehow...lets get out of here...
            return false;
        }

        try
        {
            Widget benefitsPanel = client.getWidget(206, 6);

            if (benefitsPanel != null && !benefitsPanel.isHidden())
            {
                return false;
            }

            Widget absorptionWidget = benefitsPanel.getChild(9);

            if (absorptionWidget != null && !absorptionWidget.isHidden())
            {
                return false;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            //
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Benefits Tab";
    }

    @Override
    public void onGameTick(GameTick event)
    {
		/*
		Option:	Benefits
		Target:
		Identifier:	1
		Opcode:	57
		Param0:	4 //widget.getType() ???
		Param1:	13500418
		 */
        Widget rewardsTabList = client.getWidget(206, 2);

        if (rewardsTabList == null || rewardsTabList.isHidden())
        {
            return;
        }

        Widget benefitTab;
        try
        {
            benefitTab = rewardsTabList.getChild(5);
        }
        catch (IndexOutOfBoundsException e)
        {
            //child not found somehow...lets get out of here...
            return;
        }

        if (benefitTab == null || !benefitTab.getText().equals("Benefits"))
        {
            return;
        }

        // https://github.com/Joshua-F/cs2-scripts/blob/master/scripts/%5Bclientscript,nzone_rewards_tabs%5D.cs2
        client.runScript(307, 13500418, 13500420, 13500421, 13500422, 13500422);
    }
}