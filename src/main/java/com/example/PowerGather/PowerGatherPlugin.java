package com.example.PowerGather;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.query.NPCQuery;
import com.example.EthanApiPlugin.Collections.query.TileObjectQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "Power Gather",
        description = "Will interact with an object and drop all items when inventory is full",
        tags = {"ethan", "skilling"}
)
public class PowerGatherPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private PowerGatherConfig config;
    @Inject
    private KeyManager keyManager;
    private State state;
    private boolean started;
    private int tickDelay;

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(toggle);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
    }

    @Provides
    private PowerGatherConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PowerGatherConfig.class);
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
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "PowerGather", "Delaying... ticks left: "+tickDelay, null);
            tickDelay--;
            return;
        }

        state = nextState;
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "PowerGather", "Next state: "+state.name(), null);
        // TODO: add utils with gaussian delay w/ values config
        tickDelay = ThreadLocalRandom.current().nextInt(3, 6); // reset delay
    }

    private void handleState() {
        switch (state) {
            case FIND_OBJECT:
                findObjectOrNPC();
                break;
            case DROP_ITEMS:
                dropItems();
                break;
        }
    }

    private State getNextState() {
        if (EthanApiPlugin.isMoving() || client.getLocalPlayer().getAnimation() != -1) {
            // this is to prevent clicks while animating/moving.
            return State.ANIMATING;
        }

        if (!hasTools()) {
            return State.MISSING_TOOLS;
        }

        if (isDroppingItems() && !isInventoryReset()) {
            // keep dropping until finished.
            return State.DROP_ITEMS;
        }

        if (Inventory.full()) {
            return State.DROP_ITEMS;
        }

        return State.FIND_OBJECT;
    }

    private void findObjectOrNPC() {
        String objectName = config.objectToInteract();

        TileObjects.search().withName(objectName).nearestToPlayer().ifPresent(tileObject -> {
            ObjectComposition comp = TileObjectQuery.getObjectComposition(tileObject);
            TileObjectInteraction.interact(tileObject, comp.getActions()[0]); // find the object we're looking for.  this specific example will only work if the first Action the object has is the one that interacts with it.
            // don't *always* do this, you can manually type the possible actions. eg. "Mine", "Chop", "Cook", "Climb".
        });

        NPCs.search().withName(objectName).nearestToPlayer().ifPresent(npcObject -> {
            NPCComposition comp = NPCQuery.getNPCComposition(npcObject);
            NPCInteraction.interact(npcObject, comp.getActions()[0]); // find the object we're looking for.  this specific example will only work if the first Action the object has is the one that interacts with it.
            // don't *always* do this, you can manually type the possible actions. eg. "Mine", "Chop", "Cook", "Climb".
        });
    }

    private void dropItems() {
        List<Widget> itemsToDrop = Inventory.search()
                .filter(item -> !shouldKeep(item.getName()) && !isTool(item.getName())).result(); // filter the inventory to only get the items we want to drop

        // Random number of dropped items per tick, dropItems() call
        int dropItemsPerTick = ThreadLocalRandom.current().nextInt(0, 8);
        for (int i = 0; i < Math.min(itemsToDrop.size(), dropItemsPerTick); i++) {
            InventoryInteraction.useItem(itemsToDrop.get(i), "Drop");
        }
    }

    private boolean isInventoryReset() {
        return Inventory.search()
                .filter(item -> !shouldKeep(item.getName()) && !isTool(item.getName())) // using our shouldKeep method, we can filter the items here to only include the ones we want to drop.
                .empty();
    }

    private boolean isDroppingItems() {
        return state == State.DROP_ITEMS; // if the user is dropping items, we don't want it to proceed until they're all dropped.
    }

    private boolean shouldKeep(String name) {
        String[] itemsToKeep = config.itemsToKeep().split(","); // split the items listed by comma, no space.

        return Arrays.stream(itemsToKeep) // stream the array using Arrays.stream() from java.util
                .anyMatch(i -> name.toLowerCase().contains(i.toLowerCase())); // we'll set everything to lowercase, and check if the input name contains any of the items in the itemsToKeep array.
        // might seem silly, but this is to allow specific items you want to keep without typing the full name.  I also prefer names over ids- you can change this if you like.
    }

    private boolean hasTools() {
        String[] tools = config.toolsToUse().split(","); // split the tools listed by comma, no space.

        int numInventoryTools = Inventory.search()
                .filter(item -> isTool(item.getName())) // filter inventory by using out isTool method
                .result().size();
        int numEquippedTools = Equipment.search()
                .filter(item -> isTool(item.getName())) // filter inventory by using out isTool method
                .result().size();

        return numInventoryTools + numEquippedTools >= tools.length; // if the size of tools and the filtered inventory is the same, we have our tools.
    }

    private boolean isTool(String name) {
        String[] tools = config.toolsToUse().split(","); // split the tools listed by comma, no space.

        return Arrays.stream(tools) // stream the array using Arrays.stream() from java.util
                .anyMatch(i -> name.toLowerCase().contains(i.toLowerCase())); // more likely for user error than the shouldKeep option, but we'll follow the same idea as shouldKeep.
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
        state = State.FIND_OBJECT;
    }
}
