package com.impact.NightmareZone.Tasks;

import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class WithdrawAbsorptionTask extends Task
{
    public WithdrawAbsorptionTask(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //fail if:

        //not in the nightmare zone
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
        if (Utils.getAbsorptionDoseCount(client) >= config.absorptionDoses())
            return false;

        Widget chatTitle = client.getWidget(WidgetInfo.CHATBOX_TITLE);

        if (chatTitle == null || chatTitle.isHidden())
        {
            return false;
        }

        return chatTitle.getText().contains("How many doses of absorption potion will you withdraw?");
    }

    @Override
    public String getTaskDescription()
    {
        return "Withdrawing Absorptions";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        client.setVarcIntValue(VarClientInt.INPUT_TYPE, 7);
        client.setVarcStrValue(VarClientStr.INPUT_TEXT, String.valueOf(config.absorptionDoses() - Utils.getAbsorptionDoseCount(client)));
        client.runScript(681);
        // TODO: is layer close needed, next task will trigger
        client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 1);
    }
}