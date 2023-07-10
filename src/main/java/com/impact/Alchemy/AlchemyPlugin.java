package com.impact.Alchemy;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Alchemy</font></html>",
        description = "Casts high alchemy on an item",
        tags = {"ethan", "skilling"}
)
public class AlchemyPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private AlchemyConfig config;
    @Inject
    private KeyManager keyManager;
    private State state;
    private boolean started;
    private int tickDelay;

    private enum State {
        FIND_ITEM,
        ANIMATING,
        MISSING_ITEMS,
    }

    // TODO: allow low alchemy, other spells used on items?
    static final int HIGH_ALCHEMY_WIDGET_ID = 14286888;

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(toggle);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
    }

    @Provides
    private AlchemyConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AlchemyConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!EthanApiPlugin.loggedIn() || !started) {
            // We do an early return if the user isn't logged in
            return;
        }

        State nextState = getNextState();
        if (nextState == state) {
            handleState();
            return;
        }

        if (tickDelay > 0) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Alchemy", "Delaying... ticks left: "+tickDelay, null);
            tickDelay--;
            return;
        }

        state = nextState;
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Alchemy", "Next state: "+state.name(), null);
        // TODO: add utils with gaussian delay w/ values config
        tickDelay = ThreadLocalRandom.current().nextInt(0, 2); // reset delay
    }

    private void handleState() {
        switch (state) {
            case FIND_ITEM:
                findAlchemyItem();
                break;
        }
    }

    private State getNextState() {
        if (EthanApiPlugin.isMoving() || client.getLocalPlayer().getAnimation() != -1) {
            // this is to prevent clicks while animating/moving.
            return State.ANIMATING;
        }

        if (!hasSpellItems() || !hasInteractItem()) {
            return State.MISSING_ITEMS;
        }

        return State.FIND_ITEM;
    }

    private void findAlchemyItem() {
        String itemName = config.itemToInteract();
        Optional<Widget> item = Inventory.search()
                //.filter(i -> i.getName().toLowerCase().contains(itemName.toLowerCase())) // for both lowercase
                .nameContains(itemName)
                // .first(); // TODO: can use .first() instead
                .result().stream().findFirst();

        Optional<Widget> highAlchemySpellIcon = Widgets.search().withId(HIGH_ALCHEMY_WIDGET_ID).first();
        if (highAlchemySpellIcon.isPresent() && item.isPresent()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(highAlchemySpellIcon.get(), item.get());
            tickDelay = 2; // cast delay
        }
    }

    private boolean hasSpellItems() {
        String[] items = new String[]{config.itemNatureRune(), config.itemFireRune()};

        int numInventoryItems = Inventory.search()
                .filter(item -> isItem(item.getName(), items))
                .result().size();
        int numEquippedItems = Equipment.search()
                .filter(item -> isItem(item.getName(), items))
                .result().size();

        // TODO: better way to find if items "exists", >= for now can have multiple staffs
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Alchemy", "Number of spell items: " + (numInventoryItems + numEquippedItems), null);
        return numInventoryItems + numEquippedItems >= items.length;
    }

    private boolean hasInteractItem() {
        String[] items = new String[]{config.itemToInteract()};

        int numInventoryItems = Inventory.search()
                .filter(item -> isItem(item.getName(), items))
                .result().size();

        // TODO: better way to find if item "exists", >= for now since items can be unnoted
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Alchemy", "Number of interact items: " + numInventoryItems, null);
        return numInventoryItems >= items.length;
    }

    private boolean isItem(String name, String[] items) {
        // TODO: Exact match casing for now
        return Arrays.stream(items) // stream the array using Arrays.stream() from java.util
//                .anyMatch(i -> name.toLowerCase().contains(i.toLowerCase())); // more likely for user error than the shouldKeep option, but we'll follow the same idea as shouldKeep.
                .anyMatch(i -> name.contains(i)); // more likely for user error than the shouldKeep option, but we'll follow the same idea as shouldKeep.
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
        tickDelay = 0;
        state = State.FIND_ITEM;
    }
}
