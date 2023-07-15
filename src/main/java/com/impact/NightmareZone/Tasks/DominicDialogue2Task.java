package com.impact.NightmareZone.Tasks;

import java.awt.event.KeyEvent;

import com.example.PacketUtils.WidgetInfoExtended;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import com.impact.NightmareZone.Utils;
import com.impact.NightmareZone.NightmareZoneConfig;
import com.impact.NightmareZone.NightmareZonePlugin;
import com.impact.NightmareZone.Task;

public class DominicDialogue2Task extends Task
{
    public DominicDialogue2Task(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        super(plugin, client, config);
    }

    @Override
    public boolean validate()
    {
        //check if dream is created
        if (Utils.isDreamCreated(client))
        {
            return false;
        }

        //DIALOG_OPTION_OPTION1[0] == Which dream would you like to experience?
        Widget chatTitleParent = client.getWidget(WidgetInfoExtended.DIALOG_OPTION_OPTION1.getId());

        if (chatTitleParent != null && !chatTitleParent.isHidden())
        {
            Widget chatTitleChild = chatTitleParent.getChild(0);

            return chatTitleChild.getText().contains("Agree to pay");
        }

        return false;
    }

    @Override
    public String getTaskDescription()
    {
        return "Dominic Dialogue 2";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        pressKey('1');
    }

    public void pressKey(char key)
    {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    private void keyEvent(int id, char key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );

        client.getCanvas().dispatchEvent(e);
    }
}