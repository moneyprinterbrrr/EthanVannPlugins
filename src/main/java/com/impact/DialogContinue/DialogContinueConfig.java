package com.impact.DialogContinue;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

import java.awt.*;

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

    @ConfigItem(
            keyName = "textHighlightColor",
            name = "Text highlight colour",
            description = "Color of text to select from Quest Helper",
            position = 2
    )
    default Color textHighlightColor()
    {
        return Color.BLUE;
    }
}
