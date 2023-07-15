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

public class BuyOverloadsTask extends Task
{
    public BuyOverloadsTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //in the nightmare zone
        if (Utils.isInNightmareZone(client))
            return false;

        //if we have enough overload doses in storage already
        if (client.getVarbitValue(3953) >= config.overloadDoses())
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

        Widget benefitsPanel = client.getWidget(206, 6);

        if (benefitsPanel == null || benefitsPanel.isHidden())
        {
            return false;
        }

        Widget overloadWidget = benefitsPanel.getChild(6);

        if (overloadWidget == null || overloadWidget.isHidden() || !overloadWidget.getName().equals("<col=ff9040>Overload (1)"))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Buy Overloads";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        //if amount of points is less than doses to buy * 1000
        if (client.getVar(VarPlayer.NMZ_REWARD_POINTS) / 1500 < (config.overloadDoses() - client.getVarbitValue(3953)))
        {
            plugin.stopPlugin("Not enough points to buy absorption potions!");
            return;
        }

        Widget benefitsPanel = client.getWidget(206, 6);

        if (benefitsPanel == null || benefitsPanel.isHidden())
        {
            return;
        }

        Widget overloadWidget = benefitsPanel.getChild(6);

        if (overloadWidget == null || overloadWidget.isHidden() || !overloadWidget.getName().equals("<col=ff9040>Overload (1)"))
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(overloadWidget, "Buy-50");
    }
}