package com.impact.Thiever;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.Collections.query.NPCQuery;
import com.example.EthanApiPlugin.Collections.query.TileObjectQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Thiever</font></html>",
        description = "Pickpockets NPC of your choosing, consumes food, banks",
        tags = {"ethan", "skilling"}
)
public class ThieverPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ThieverConfig config;
    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ThieverOverlay overlay;

    @Inject
    PluginManager pluginManager;

    State state;
    boolean started;
    int tickDelay;
    int numPouchesToOpen;

    private enum State {
        FIND_NPC,
        ANIMATING,
        HEAL,
        OPEN_POUCHES,
        BANK
    }

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
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
    }

    @Provides
    private ThieverConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ThieverConfig.class);
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
        tickDelay = ThreadLocalRandom.current().nextInt(1, 4); // reset delay
    }

    private void handleState() {
        switch (state) {
            case FIND_NPC:
                findNPC();
                break;
            case HEAL:
                consumeFood();
                break;
            case OPEN_POUCHES:
                openPouches();
                break;
            case BANK:
                restockFood();
                break;
        }
    }

    private State getNextState() { // Get current state, `state` field is previous state really
        if (EthanApiPlugin.isMoving() || client.getLocalPlayer().getAnimation() != -1) {
            // this is to prevent clicks while animating/moving.
            return State.ANIMATING;
        }

        if (outOfFood()) {
            return State.BANK;
        }

        if (shouldHeal()) {
            return State.HEAL;
        }

        if (shouldOpenPouches()) {
            return State.OPEN_POUCHES;
        }

        return State.FIND_NPC;
    }

    private void findNPC() {
        String npcName = config.npcToInteract();

        NPCs.search().withName(npcName).nearestToPlayer().ifPresent(npcObject -> {
            NPCComposition comp = NPCQuery.getNPCComposition(npcObject);
            NPCInteraction.interact(npcObject, "Pickpocket"); // find the object we're looking for.  this specific example will only work if the first Action the object has is the one that interacts with it.
            // don't *always* do this, you can manually type the possible actions. eg. "Mine", "Chop", "Cook", "Climb".
        });
    }

    private void consumeFood() {
        String itemName = config.foodToConsume();
        Optional<Widget> item = Inventory.search()
                //.filter(i -> i.getName().toLowerCase().contains(itemName.toLowerCase())) // for both lowercase
                .nameContains(itemName)
                // .first(); // TODO: can use .first() instead
                .result().stream().findFirst();

        // TODO: can also try...
        // MousePackets.queueClickPacket();
        // WidgetPackets.queueWidgetOnWidget(item.get(), "Eat"); // or "Drink", not sure how to get top level action
        if (item.isPresent()) {
            ItemComposition comp = ItemQuery.getItemComposition(item.get());
            InventoryInteraction.useItem(item.get(), comp.getInventoryActions()[0]);
            tickDelay = 2; // food eat delay
        }
    }

    private void openPouches() {
        String itemName = "Coin pouch"; // make static var?
        Optional<Widget> item = Inventory.search()
                //.filter(i -> i.getName().toLowerCase().contains(itemName.toLowerCase())) // for both lowercase
                .nameContains(itemName)
                // .first(); // TODO: can use .first() instead
                .result().stream().findFirst();

        // TODO: can also try...
        // MousePackets.queueClickPacket();
        // WidgetPackets.queueWidgetOnWidget(item.get(), "Open-all");
        if (item.isPresent()) {
            ItemComposition comp = ItemQuery.getItemComposition(item.get());
            InventoryInteraction.useItem(item.get(), comp.getInventoryActions()[0]);
            tickDelay = 1; // open pouch delay
            // TODO: add utils with gaussian delay w/ values config
            numPouchesToOpen = ThreadLocalRandom.current().nextInt(0, 28);
        }
    }

    // TODO: use below "close" bank in `restockFood()`, findNPC will queue a movement anyway
    // if (Bank.isOpen()) {
    //     MousePackets.queueClickPacket();
    //     MovementPackets.queueMovement(client.getLocalPlayer().getWorldLocation());
    //     return;
    // }

    private void restockFood() { // deposit all and withdraw food
        if (Bank.isOpen()) {
            Optional<Widget> foodInBank = Bank.search().nameContains(config.foodToConsume()).first();
            if (outOfFood() && foodInBank.isPresent()) {
                // Deposit everything (helps for empty jugs of wine)
                Widget widget = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(widget, "Deposit", "Deposit inventory");

                 // BankInteraction.withdrawX kept enter amount interface up...
                 BankInteraction.useItem(x -> x.getName().contains(config.foodToConsume()), "Withdraw-10");
                 BankInteraction.useItem(x -> x.getName().contains(config.foodToConsume()), "Withdraw-10");
                return;
            }
        } else {
            // Open bank
            Optional<TileObject> bankBooth = findBank();
            if (bankBooth.isPresent()) {
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(bankBooth.get(), "Bank");
                // TODO: add utils with gaussian delay w/ values config
                tickDelay = ThreadLocalRandom.current().nextInt(2, 6);
            }
        }
    }

    private Optional<TileObject> findBank() {
        // TODO: EthanAPI.findObject() instead ?
        Optional<TileObject> bankBooth = TileObjects.search().filter(tileObject -> {
            ObjectComposition objectComposition = TileObjectQuery.getObjectComposition(tileObject);
            return getName().contains("Bank") ||
                    Arrays.stream(objectComposition.getActions()).anyMatch(action -> action != null && action.contains("Bank"));
        }).nearestToPlayer();
        return bankBooth;
    }

    private boolean outOfFood() {
        String itemName = config.foodToConsume();
        return Inventory.search()
                .filter(item -> item.getName().contains(itemName))
                .empty();
    }

    private boolean shouldHeal() {
        int hitpointNoise = ThreadLocalRandom.current().nextInt(1, 14);
        int hitpointToHeal = config.hitpointThreshold() + hitpointNoise;
        return client.getBoostedSkillLevel(Skill.HITPOINTS) <= hitpointToHeal;
    }

    private boolean shouldOpenPouches() {
        String itemName = "Coin pouch"; // make static var?
        return !Inventory.search().nameContains(itemName).quantityGreaterThan(numPouchesToOpen).empty();
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
        // TODO: add utils with gaussian delay w/ values config
        numPouchesToOpen = ThreadLocalRandom.current().nextInt(0, 28);
        state = State.FIND_NPC;
    }
}
