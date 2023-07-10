package com.impact.LavaCrafter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("LavaCrafter")
public interface LavaCrafterConfig extends Config {
	/*@ConfigItem(
		position = 0,
		keyName = "optionsTitle",
		name = "Options",
		description = ""
	)
	default Title optionsTitle() { return new Title(); }

	@ConfigItem(
		position = 1,
		keyName = "useSmallPouch",
		name = "Use Small Pouch",
		description = ""
	)
	default boolean useSmallPouch() { return false; }

	@ConfigItem(
		position = 2,
		keyName = "useMediumPouch",
		name = "Use Medium Pouch",
		description = ""
	)
	default boolean useMediumPouch() { return false; }

	@ConfigItem(
		position = 3,
		keyName = "useLargePouch",
		name = "Use Large Pouch",
		description = ""
	)
	default boolean useLargePouch() { return false; }

	@ConfigItem(
		position = 4,
		keyName = "useGiantPouch",
		name = "Use Giant Pouch",
		description = ""
	)
	default boolean useGiantPouch() { return false; } */

    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = "",
            position = 0
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            position = 5,
            keyName = "useBindingNecklace",
            name = "Use Binding Necklace",
            description = ""
    )
    default boolean useBindingNecklace() { return true; }

    @ConfigItem(
            position = 6,
            keyName = "autoEnableRun",
            name = "Auto Enable Run",
            description = ""
    )
    default boolean autoEnableRun() { return true; }

    @ConfigItem(
            position = 7,
            keyName = "useMagicImbue",
            name = "Use Magic Imbue",
            description = "if disabled, will withdraw & use earth talismans instead"
    )
    default boolean useMagicImbue() { return false; }

    @ConfigItem(
            position = 8,
            keyName = "disablePaint",
            name = "Disable Paint",
            description = "will disable drawing anything on screen"
    )
    default boolean disablePaint() { return false; }

    @ConfigItem(
            position = 10,
            keyName = "useLevelStopCondition",
            name = "Use Level Stop Condition",
            description = ""
    )
    default boolean useLevelStopCondition() { return false; }

    @ConfigItem(
            position = 11,
            keyName = "levelStopConditionValue",
            name = "Level To Stop at",
            description = ""
    )
    default int levelStopConditionValue() { return 99; }

    @ConfigItem(
            position = 12,
            keyName = "useTimeStopCondition",
            name = "Use Time Stop Condition",
            description = ""
    )
    default boolean useTimeStopCondition() { return false; }

    @ConfigItem(
            position = 13,
            keyName = "timeStopConditionValue",
            name = "Minutes To Run For",
            description = ""
    )
    default int timeStopConditionValue() { return 60; }

    @ConfigItem(
            position = 15,
            keyName = "tickDelayMin",
            name = "Tick Delay Min",
            description = "The minimum tick delay between clicks"
    )
    default int clickDelayMin() { return 1; }

    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Tick Delay Max",
            description = "The maximum tick delay between clicks",
            position = 16
    )
    default int clickDelayMax() { return 5; }
}
