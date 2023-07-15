package com.impact.NightmareZone.Tasks;

import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class AcceptDreamTask extends Task
{
    public AcceptDreamTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //nmz dream accept button
        // TODO: double check widget id's
        Widget acceptWidget = client.getWidget(129, 6);

        return acceptWidget != null && !acceptWidget.isHidden();
    }

    @Override
    public String getTaskDescription()
    {
        return "Accepting Dream";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Widget acceptWidget = client.getWidget(129, 6);

        if (acceptWidget == null || acceptWidget.isHidden())
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(acceptWidget.getId(), -1);
    }
}