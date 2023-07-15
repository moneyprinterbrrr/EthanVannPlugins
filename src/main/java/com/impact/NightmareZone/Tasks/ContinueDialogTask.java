package com.impact.NightmareZone.Tasks;


import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import com.example.PacketUtils.WidgetInfoExtended;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class ContinueDialogTask extends Task
{
    public ContinueDialogTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //in the nightmare zone
        if (Utils.isInNightmareZone(client))
            return false;

        Widget widget = client.getWidget(WidgetInfoExtended.DIALOG_NPC_CONTINUE.getId());

        return widget != null && !widget.isHidden();
    }

    @Override
    public String getTaskDescription()
    {
        return "Continuing Dialog";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Widget widget = client.getWidget(WidgetInfoExtended.DIALOG_NPC_CONTINUE.getId());

        if (widget == null || widget.isHidden())
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(widget.getId(), -1);
    }
}