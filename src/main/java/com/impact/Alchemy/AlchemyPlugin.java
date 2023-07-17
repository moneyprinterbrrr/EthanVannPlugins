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
import net.runelite.client.ui.overlay.OverlayManager;
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

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AlchemyOverlay overlay;

    State state;
    boolean started;
    int tickDelay;

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
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
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
            tickDelay--;
            return;
        }

        state = nextState;
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
        // TODO: allow alching when running
//        boolean isIdle = !EthanApiPlugin.isMoving() || client.getLocalPlayer().getAnimation() == -1;
//        boolean isWalking = client.getLocalPlayer().getAnimation() == client.getLocalPlayer().getWalkAnimation();
//        boolean isRunning = client.getLocalPlayer().getAnimation() == client.getLocalPlayer().getRunAnimation();
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
        String[] alchemyItems = config.itemToInteract().split(",");
        Optional<Widget> singleItem = Inventory.search()
                .filter(item -> isItem(item.getName(), alchemyItems))
                .first();

        Optional<Widget> highAlchemySpellIcon = Widgets.search().withId(HIGH_ALCHEMY_WIDGET_ID).first();
        if (highAlchemySpellIcon.isPresent() && singleItem.isPresent()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(highAlchemySpellIcon.get(), singleItem.get());
            tickDelay = 2; // cast delay, TODO: change to 5, when fixing ANIMATE state
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
        return numInventoryItems + numEquippedItems >= items.length;
    }

    private boolean hasInteractItem() {
        String[] items = config.itemToInteract().split(",");

        int numInventoryItems = Inventory.search()
                .filter(item -> isItem(item.getName(), items))
                .result().size();

        return numInventoryItems >= 1;
    }

    private boolean isItem(String name, String[] items) {
        // TODO: Exact match casing for now
        return Arrays.stream(items)
                .anyMatch(i -> name.contains(i));
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
