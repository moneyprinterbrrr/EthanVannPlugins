package com.impact.Thiever;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Theiver")
public interface ThieverConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            name = "NPC",
            keyName = "npcToInteract",
            description = "NPC you will be interacting with",
            position = 0
    )
    default String npcToInteract() {
        return "Knight of Ardougne";
    }

    @ConfigItem(
            name = "Food",
            keyName = "foodToConsume",
            description = "Food to consume when health is low",
            position = 1
    )
    default String foodToConsume() {
        return "Jug of wine";
    }

    @ConfigItem(
            name = "Hitpoint threshold",
            keyName = "hitpointThreshold",
            description = "Minimum hitpoint threshold to trigger food consumption",
            position = 2
    )
    default int hitpointThreshold() {
        return 11;
    }
}

