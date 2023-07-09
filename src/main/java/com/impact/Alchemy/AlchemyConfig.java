package com.impact.Alchemy;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Alchemy")
public interface AlchemyConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = ""
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            name = "Fire Rune Item",
            keyName = "itemFireRune",
            description = "Fire rune providing item: Fire rune, Staff of Fire, Tome of Fire",
            position = 0
    )
    default String itemFireRune() {
        return "Staff of fire";
    }

    @ConfigItem(
            name = "Nature Rune Item",
            keyName = "itemNatureRune",
            description = "Fire rune providing item: Nature rune, Bryophyta's staff",
            position = 1
    )
    default String itemNatureRune() {
        return "Nature rune";
    }

    @ConfigItem(
            name = "Alchemy Item",
            keyName = "itemToInteract",
            description = "Game item you will be interacting with",
            position = 2
    )
    default String itemToInteract() {
        return "Rune 2h sword";
    }
}
