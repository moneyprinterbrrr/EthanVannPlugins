package com.impact.NightmareZone;

import java.util.Arrays;
import java.util.List;

import com.example.EthanApiPlugin.Collections.Inventory;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

public class Utils
{
    private static final int[] NMZ_MAP_REGION = {9033};

    public static final int SPECIAL_ATTACK_WIDGET_ID = 38862884;

    public static boolean isInNightmareZone(Client client)
    {
        if (client.getLocalPlayer() == null)
        {
            return false;
        }

        // NMZ and the KBD lair uses the same region ID but NMZ uses planes 1-3 and KBD uses plane 0
        return client.getLocalPlayer().getWorldLocation().getPlane() > 0 && Arrays.equals(client.getMapRegions(), NMZ_MAP_REGION);
    }

    public static boolean isDreamCreated(Client client)
    {
        return client.getVarbitValue(3946) == 123;
    }

    public static int getAbsorptionDoseCount(Client client)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return 0;
        }

        List<Widget> result = Inventory.search()
                .idInList(Arrays.asList(ItemID.ABSORPTION_1, ItemID.ABSORPTION_2,
                        ItemID.ABSORPTION_3, ItemID.ABSORPTION_4))
                .result();

        if (result.isEmpty())
            return 0;

        int doseCount = (int) result.stream().filter(item -> item.getItemId() == ItemID.ABSORPTION_1).count();
        doseCount += 2 * (int) result.stream().filter(item -> item.getItemId() == ItemID.ABSORPTION_2).count();
        doseCount += 3 * (int) result.stream().filter(item -> item.getItemId() == ItemID.ABSORPTION_3).count();
        doseCount += 4 * (int) result.stream().filter(item -> item.getItemId() == ItemID.ABSORPTION_4).count();

        return doseCount;
    }

    public static int getOverloadDoseCount(Client client)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return 0;
        }

        List<Widget> result = Inventory.search()
                .idInList(Arrays.asList(ItemID.OVERLOAD_1, ItemID.OVERLOAD_2,
                        ItemID.OVERLOAD_3, ItemID.OVERLOAD_4))
                .result();

        if (result.isEmpty())
            return 0;

        int doseCount = (int) result.stream().filter(item -> item.getItemId() == ItemID.OVERLOAD_1).count();
        doseCount += 2 * (int) result.stream().filter(item -> item.getItemId() == ItemID.OVERLOAD_2).count();
        doseCount += 3 * (int) result.stream().filter(item -> item.getItemId() == ItemID.OVERLOAD_3).count();
        doseCount += 4 * (int) result.stream().filter(item -> item.getItemId() == ItemID.OVERLOAD_4).count();

        return doseCount;
    }
}