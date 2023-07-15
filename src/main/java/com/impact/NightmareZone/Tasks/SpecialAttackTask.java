package com.impact.NightmareZone.Tasks;

import java.util.Random;

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

public class SpecialAttackTask extends Task
{
    private final Random r = new Random();

    int nextSpecialValue = r.nextInt(config.specialAttackMax() - config.specialAttackMin()) + config.specialAttackMin();

    public SpecialAttackTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //option is disabled in config
        if (!config.useSpecialAttack())
        {
            return false;
        }

        //not in the nightmare zone
        if (!Utils.isInNightmareZone(client))
        {
            return false;
        }

        //spec already enabled
        if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1)
        {
            return false;
        }

        //value returns 1000 for 100% spec, 500 for 50%, etc
        if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) < nextSpecialValue * 10)
        {
            return false;
        }

        Widget specialOrb = client.getWidget(160, 30);

        if (specialOrb == null || specialOrb.isHidden())
        {
            return false;
        }

        return true;
    }

    @Override
    public String getTaskDescription()
    {
        return "Use Special Attack";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, Utils.SPECIAL_ATTACK_WIDGET_ID, -1, -1);

        nextSpecialValue = r.nextInt(config.specialAttackMax() - config.specialAttackMin()) + config.specialAttackMin();
    }
}