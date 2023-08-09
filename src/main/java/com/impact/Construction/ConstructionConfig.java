package com.impact.Construction;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Construction")
public interface ConstructionConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            name = "Product",
            keyName = "product",
            description = "Product to build",
            position = 0
    )
    default Product product() {
        return Product.OAK_TABLE;
    }

    @ConfigItem(
            name = "Method",
            keyName = "method",
            description = "Method to use: servant or unnote with Phials",
            position = 1
    )
    default Method method() {
        return Method.PHIALS;
    }

    @ConfigItem(
            keyName = "autoEnableRun",
            name = "Auto Enable Run",
            description = "Enables run",
            position = 6
    )
    default boolean autoEnableRun() { return true; }

    @ConfigItem(
            name = "Disable Paint",
            keyName = "disablePaint",
            description = "will disable drawing anything on screen",
            position = 3
    )
    default boolean disablePaint() { return false; }
}
