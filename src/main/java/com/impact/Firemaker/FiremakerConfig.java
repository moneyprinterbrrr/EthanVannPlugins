package com.impact.Firemaker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Alchemy")
public interface FiremakerConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            name = "Log Item",
            keyName = "itemToBurn",
            description = "Log to burn: Logs, Oak logs, etc.",
            position = 0
    )
    default String itemToBurn() {
        return "Logs";
    }

    // TODO: For now walk, need to add missing teleport runes/tabs etc
    @ConfigItem(
            name = "Varrock Teleport",
            keyName = "useTeleport",
            description = "Walk or teleport to Varrock center",
            position = 1
    )
    default boolean itemNatureRune() {
        return false;
    }

    @ConfigItem(
            name = "Disable Paint",
            keyName = "disablePaint",
            description = "will disable drawing anything on screen",
            position = 2
    )
    default boolean disablePaint() { return false; }
}
