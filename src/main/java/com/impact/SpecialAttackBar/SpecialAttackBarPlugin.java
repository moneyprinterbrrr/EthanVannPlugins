package com.impact.SpecialAttackBar;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PacketUtils.WidgetInfoExtended;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Special Attack</font></html>",
        description = "Shows special attack bar for all weapons",
        tags = {"ethan", "pvp"},
        enabledByDefault = false
)
public class SpecialAttackBarPlugin extends Plugin{
    @Inject
    private Client client;

    private static final int SPECIAL_ATTACK_WIDGET_ID = 593;

    private static final int SPECIAL_ATTACK_WIDTH = 146;
    private static final int SPECIAL_ATTACK_HEIGHT = 12;

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        Widget specialAttackBar = client.getWidget(SPECIAL_ATTACK_WIDGET_ID,35);
        Widget specialAttackBarOrnaments = client.getWidget(SPECIAL_ATTACK_WIDGET_ID,36);
        Widget specialAttackBarText = client.getWidget(SPECIAL_ATTACK_WIDGET_ID,40);
        Widget specBarChargeBarInner = client.getWidget(SPECIAL_ATTACK_WIDGET_ID,38);
        Widget specBarChargeBarInnerComponent = client.getWidget(SPECIAL_ATTACK_WIDGET_ID,39);

        if (specialAttackBar != null && specialAttackBarOrnaments != null && specialAttackBarText != null
                && specBarChargeBarInner != null && specBarChargeBarInnerComponent != null)
        {
            specialAttackBar.setHidden(false);
            for (Widget ornament : specialAttackBarOrnaments.getChildren())
                ornament.setHidden(false);
            specialAttackBarText.setHidden(false);

            int currentSpecValue = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
            specialAttackBarText.setText("Special Attack: " + currentSpecValue + "%");
            int currentChargeBarWidth = SPECIAL_ATTACK_WIDTH * currentSpecValue / 100;
            specBarChargeBarInner.setSize(currentChargeBarWidth,SPECIAL_ATTACK_HEIGHT);
            specBarChargeBarInner.revalidate();
            specBarChargeBarInnerComponent.revalidate();
        }
    }
}


