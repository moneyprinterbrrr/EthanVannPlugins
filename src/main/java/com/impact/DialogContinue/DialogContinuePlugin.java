package com.impact.DialogContinue;

import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PacketUtils.WidgetID;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.*;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Dialog Continue</font></html>",
        description = "Skips dialog, including quest dialog",
        tags = {"ethan", "dialog", "questing"}
)
public class DialogContinuePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private DialogContinueConfig config;

    @Inject
    PluginManager pluginManager;

    private boolean questHelperContinueTriggered;

    private boolean continueTriggered;

    private static final List<Integer> DIALOG_WIDGET_GROUP_TYPES = List.of(
            WidgetID.DIALOG_MINIGAME_GROUP_ID,
            WidgetID.DIALOG_NOTIFICATION_GROUP_ID,
            WidgetID.DIALOG_NPC_GROUP_ID,
            WidgetID.DIALOG_OPTION_GROUP_ID,
            WidgetID.DIALOG_PLAYER_GROUP_ID,
            WidgetID.DIALOG_SPRITE2_ID,
            WidgetID.DIALOG_SPRITE_GROUP_ID
    );

    @Override
    @SneakyThrows
    protected void startUp() throws Exception {
        if (client.getRevision() != PacketUtilsPlugin.CLIENT_REV) {
            SwingUtilities.invokeLater(() ->
            {
                try {
                    pluginManager.setPluginEnabled(this, false);
                    pluginManager.stopPlugin(this);
                } catch (PluginInstantiationException ignored) {
                }
            });
            return;
        }
        questHelperContinueTriggered = false;
        continueTriggered = false;
    }

    @Override
    protected void shutDown() throws Exception {
        questHelperContinueTriggered = false;
        continueTriggered = false;
    }

    @Provides
    private DialogContinueConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(DialogContinueConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        // not logged in
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (continueTriggered) {
            clickToContinue();
            return;
        }

        if (questHelperContinueTriggered) {
            questHelperContinue();
            return;
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        // not logged in
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        // Not a dialog related widget
        if (!DIALOG_WIDGET_GROUP_TYPES.contains(Integer.valueOf(event.getGroupId()))) {
            return;
        }

        if (config.questHelper() && event.getGroupId() == WidgetID.DIALOG_OPTION_GROUP_ID) {
            questHelperContinueTriggered = true;
            return;
        }

        if (event.getGroupId() != WidgetID.DIALOG_OPTION_GROUP_ID) {
            continueTriggered = true;
            return;
        }
    }

    private void clickToContinue() {
        Optional<Widget> continueWidget = Widgets.search().hiddenState(false)
                .withTextContains("Click here to continue")
                .first();

        // No continue option
        if (!continueWidget.isPresent()) {
            return;
        }

        if (continueWidget.get().getActions() != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(continueWidget.get().getId(), 0);
            continueTriggered = false;
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(continueWidget.get().getId(), -1);
        continueTriggered = false;
    }

    private void questHelperContinue() {
        Widgets.search().withId(WidgetInfo.DIALOG_OPTION_OPTIONS.getId()).hiddenState(false).result().forEach(option -> {
            if (option.getTextColor() == config.textHighlightColor().getRGB()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(option.getId(), option.getIndex());
                questHelperContinueTriggered = false;
            }
        });
    }
}
