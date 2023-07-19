package com.impact.NightmareZone;

import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.impact.NightmareZone.Tasks.*;
import lombok.SneakyThrows;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Singleton
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Nightmare Zone</font></html>",
        description = "Nightmare Zone automation",
        tags = {"nmz", "nightmare", "zone"},
        enabledByDefault = false
)
public class NightmareZonePlugin extends Plugin
{
	/*
		varbits
		absorptions - 3954 (doses in storage)
		overloads - 3953 (doses in storage)
	 */

    static List<Class<?>> taskClassList = new ArrayList<>();

    static
    {
        taskClassList.add(SpecialAttackTask.class);
        taskClassList.add(OverloadTask.class);
        taskClassList.add(AbsorptionTask.class);
        taskClassList.add(RockCakeTask.class);
        taskClassList.add(PowerSurgeTask.class);

        //--------------------------
        taskClassList.add(SearchRewardsChestTask.class);
        taskClassList.add(BenefitsTabTask.class);
        taskClassList.add(BuyAbsorptionsTask.class);
        taskClassList.add(BuyOverloadsTask.class);
        //--------------------------

        taskClassList.add(WithdrawAbsorptionTask.class);
        taskClassList.add(WithdrawOverloadTask.class);
        taskClassList.add(OpenAbsorptionsBarrelTask.class);
        taskClassList.add(OpenOverloadsBarrel.class);

        taskClassList.add(DominicDreamTask.class);
        taskClassList.add(DominicDialogue1Task.class);
        taskClassList.add(DominicDialogue2Task.class);

        taskClassList.add(ContinueDialogTask.class);

        taskClassList.add(DrinkPotionTask.class);
        taskClassList.add(AcceptDreamTask.class);
    }

    @Inject
    private Client client;

    @Inject
    private NightmareZoneConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NightmareZoneOverlay overlay;

    @Inject
    PluginManager pluginManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    boolean started;

    @Provides
    NightmareZoneConfig provideConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(NightmareZoneConfig.class);
    }

    public String status = "Initializing...";

    private final TaskSet tasks = new TaskSet();

    public static int rockCakeDelay = 0;

    @Override
    @SneakyThrows
    protected void startUp() throws Exception
    {
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
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        status = "Initializing...";
        tasks.clear();
        tasks.addAll(this, client, config, taskClassList);
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
        tasks.clear();
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void toggle() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        started = !started;
    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        if (!started)
        {
            return;
        }

        String msg = Text.removeTags(event.getMessage()); //remove color

        switch (event.getType())
        {
            case SPAM:
                if (msg.contains("You drink some of your overload potion."))
                {
                    rockCakeDelay = 12;
                }
                break;
            case GAMEMESSAGE:
                if (msg.contains("This barrel is empty.")
                        || msg.contains("There is no ammo left in your quiver.")
                        || msg.contains("Your blowpipe has run out of scales and darts.")
                        || msg.contains("Your blowpipe has run out of darts.")
                        || msg.contains("Your blowpipe needs to be charged with Zulrah's scales."))
                {
                    stopPlugin("Received game message: " + msg);
                }
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!started)
        {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        //if we don't have a rock cake, return...may need to stop the plugin but this is causing it to stop
        //	randomly for some reason
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
            return;

        if (Inventory.search().withId(ItemID.DWARVEN_ROCK_CAKE_7510).empty())
        {
            //started = false;
            sendGameMessage("Rock cake not found...");
            return;
        }

        if (client.getVarbitValue(3948) < 26 && !Utils.isInNightmareZone(client))
        {
            stopPlugin("You need to put money in the coffer!");
            return;
        }

        Task task = tasks.getValidTask();

        if (task != null)
        {
            status = task.getTaskDescription();
            task.onGameTick(event);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (!started)
            return;

        if (!config.autoRelog())
            return;

        if (client.getGameState() != GameState.LOGIN_SCREEN)
            return;

        client.setUsername(config.email());
        client.setPassword(config.password());
        client.setGameState(GameState.LOGGING_IN);
    }

    private void sendGameMessage(String message)
    {
        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(
                                new ChatMessageBuilder()
                                        .append(ChatColorType.HIGHLIGHT)
                                        .append(message)
                                        .build())
                        .build());
    }

    public void stopPlugin()
    {
        stopPlugin("");
    }

    public void stopPlugin(String reason)
    {
        started = false;

        if (reason != null && !reason.isEmpty())
            sendGameMessage("NightmareZone Plugin Stopped: " + reason);
    }
}