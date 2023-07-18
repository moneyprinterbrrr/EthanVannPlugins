package com.impact.ReorderPrayers;

import net.runelite.api.Prayer;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ReorderPrayersPlugin.CONFIG_GROUP_KEY)
public interface ReorderPrayersConfig extends Config
{

    @ConfigItem(
            keyName = ReorderPrayersPlugin.CONFIG_UNLOCK_REORDERING_KEY,
            name = "Unlock Prayer Reordering",
            description = "Configures whether or not you can reorder the prayers",
            position = 1
    )
    default boolean unlockPrayerReordering()
    {
        return false;
    }

    @ConfigItem(
            keyName = ReorderPrayersPlugin.CONFIG_UNLOCK_REORDERING_KEY,
            name = "",
            description = ""
    )
    void unlockPrayerReordering(boolean unlock);

    @ConfigItem(
            keyName = ReorderPrayersPlugin.CONFIG_PRAYER_ORDER_KEY,
            name = "Prayer Order",
            description = "Configures the order of the prayers",
            hidden = true,
            position = 2
    )
    default String prayerOrder()
    {
        return ReorderPrayersPlugin.prayerOrderToString(Prayer.values());
    }

    @ConfigItem(
            keyName = ReorderPrayersPlugin.CONFIG_PRAYER_ORDER_KEY,
            name = "",
            description = ""
    )
    void prayerOrder(String prayerOrder);
}