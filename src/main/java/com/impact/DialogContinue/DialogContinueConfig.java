package com.impact.DialogContinue;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("DialogContinue")
public interface DialogContinueConfig extends Config {
    @ConfigItem(
            name = "Quest Helper",
            keyName = "questHelper",
            description = "Skip through quest helper highlighted options",
            position = 0
    )
    default boolean questHelper() {
        return false;
    }
}
