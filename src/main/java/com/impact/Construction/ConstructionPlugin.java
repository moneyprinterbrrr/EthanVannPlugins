package com.impact.Construction;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.awt.event.KeyEvent;
import java.util.Optional;

@Slf4j
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Construction</font></html>",
        description = "Builds desired object repeatedly, calls butler for supplies",
        tags = {"ethan", "skilling"}
)
public class ConstructionPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ConstructionConfig config;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConstructionOverlay overlay;

    @Inject
    PluginManager pluginManager;

    State state;
    boolean started;
    int tickDelay;

    private static final int CALL_SERVANT_WIDGET_ID = 24248342;
    private static final int IN_HOUSE_BUILD_MODE_VARBIT = 2176;
    private static final int BUILD_INTERFACE_WIDGET_ID = 30015489; // or 30015493?

    // TODO: account for butler delay, so fetch planks mid inventory?
    //       when empty slots >= number of planks per product * 2
    private enum State {
        FIND_BUILD_SPACE,
        BUILD, // interface
        REMOVE,
        REMOVE_DIALOG,
        CALL_SERVANT, // TODO: not tested
        SERVANT_DIALOG_CONTINUE, // TODO: not implemented
        SERVANT_DIALOG_FETCH_OPTION, // TODO: not implemented
        WAITING_FOR_PLANKS,
        LEAVE_HOUSE,
        UNNOTE_PHIALS,
        UNNOTE_PHIALS_DIALOG,
        ENTER_HOUSE,
        ANIMATING,
        MISSING_TOOLS,
    }

    @Provides
    private ConstructionConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ConstructionConfig.class);
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
        started = false;
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
        state = State.FIND_BUILD_SPACE;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!EthanApiPlugin.loggedIn() || !started) {
            // We do an early return if the user isn't logged in
            return;
        }

        state = getState();
        handleState();
    }

    private State getState() {
        if (EthanApiPlugin.isMoving() || client.getLocalPlayer().getAnimation() != -1) {
            return State.ANIMATING;
        }

        if (!hasTools()) {
            return State.MISSING_TOOLS;
        }

        if (!hasEnoughUnnotedPlanks()) {
            return getMorePlanksState();
        }

        if (!inBuildModeHouse()) {
            return State.ENTER_HOUSE;
        }

        if (hasBuildInterface()) {
            return State.BUILD;
        }

        if (state == State.REMOVE) {
            return State.REMOVE_DIALOG;
        }

        if (hasBuilt()) {
            return State.REMOVE;
        }

        return State.FIND_BUILD_SPACE;
    }

    private State getMorePlanksState() {
        if (config.method() == Method.SERVANT) {
            if (state != State.CALL_SERVANT && state != State.WAITING_FOR_PLANKS) {
                return State.CALL_SERVANT;
            }
            return State.WAITING_FOR_PLANKS;
        } else { // Method.PHIALS
            if (inBuildModeHouse()) {
                return State.LEAVE_HOUSE;
            }
            if (isTalkingToPhials()) {
                return State.UNNOTE_PHIALS_DIALOG;
            }
            return State.UNNOTE_PHIALS;
        }
    }

    private void handleState() {
        switch (state) {
            case FIND_BUILD_SPACE:
                findBuildSpace();
                break;
            case BUILD:
                buildProduct();
                break;
            case REMOVE:
                removeProduct();
                break;
            case REMOVE_DIALOG:
                removeDialog();
                break;
            case CALL_SERVANT:
                callServant();
                break;
            case LEAVE_HOUSE:
                leaveHouse();
                break;
            case UNNOTE_PHIALS:
                unnotePlanks();
                break;
            case UNNOTE_PHIALS_DIALOG:
                unnotePlanksDialog();
                break;
            case ENTER_HOUSE:
                enterHouse();
                break;
            default: // ANIMATE, WAITING_FOR_PLANKS, MISSING_TOOLS
                break;
        }
    }

    private void findBuildSpace() {
        Product product = config.product();
        TileObjects.search().withId(product.getBuildSpaceId()).nearestToPlayer().ifPresent(tileObject -> {
            TileObjectInteraction.interact(tileObject, "Build");
        });
    }

    private void buildProduct() {
        Product product = config.product();
        pressKey(Integer.toString(product.getIndex()).charAt(0));

        // [script_id, garbage value, build option (#2), someId?, garbage value, constant?]
        // Oak Larder: OnOpListener	[1405, -2147483644, 2, 8234, -2147483645, 4]
        // Oak Table: OnOpListener	[1405, -2147483644, 2, 8116, -2147483645, 4]
        // client.runScript(); //???

        // All construction build menu widget options start at: 458.4
        // wooden larder widget: 458.4 -> 30015492
        // oak larder widget: 458.5 -> 30015493
        // ... dont think packets work either
    }

    private void removeProduct() {
        // Remove
        Product product = config.product();
        TileObjects.search().withId(product.getRemoveSpaceId()).nearestToPlayer().ifPresent(tileObject -> {
            TileObjectInteraction.interact(tileObject, "Remove");
        });
    }

    private void removeDialog() {
        // Confirm chat message
        Widgets.search().withId(WidgetInfo.DIALOG_OPTION_OPTIONS.getId()).result().forEach(option -> {
            if (option.getText().contains("Yes")) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(option.getId(), option.getIndex());
            }
        });
    }

    private void callServant() {
        // TODO: dont think this is a client script
        // TODO: not tested...
        Widgets.search().withId(CALL_SERVANT_WIDGET_ID).first().ifPresent(widget -> {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(widget.getId(), -1);
        });

        // continue dialog
        Widgets.search().withId(WidgetInfoExtended.DIALOG_NPC_CONTINUE.getId()).first().ifPresent(widget -> {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(widget.getId(), -1);
        });

        // Ask for planks
        Widgets.search().withId(WidgetInfo.DIALOG_OPTION_OPTIONS.getId()).result().forEach(option -> {
            if (option.getText().contains("Yes")) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(option.getId(), option.getIndex());
            }
        });
    }

    private void leaveHouse() {
        // 4525 is build mode house portal
        Optional<TileObject> portal = TileObjects.search().withId(ObjectID.PORTAL_4525).first();
        if (portal.isPresent()){
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(ObjectID.PORTAL_4525, "Enter");
        }
    }

    private void unnotePlanks() {
        Product product = config.product();
        int notedPlankId = product.getPlankId() + 1; // TODO: is this always true!?
        Optional<Widget> planks = Inventory.search().onlyNoted().withId(notedPlankId).first();
        Optional<NPC> phials = NPCs.search().withId(NpcID.PHIALS).first();
        if (planks.isPresent() && phials.isPresent()) {
            MousePackets.queueClickPacket();
            NPCPackets.queueWidgetOnNPC(phials.get(), planks.get());
        }
    }

    private void unnotePlanksDialog() {
        // Unnote all
        Widgets.search().withId(WidgetInfo.DIALOG_OPTION_OPTIONS.getId()).result().forEach(option -> {
            if (option.getText().contains("Exchange All")) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(option.getId(), option.getIndex());
            }
        });
    }

    private void enterHouse() {
        // 15478 is rimmington house portal
        Optional<TileObject> portal = TileObjects.search().withId(ObjectID.PORTAL_15478).first();
        if (portal.isPresent()){
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(ObjectID.PORTAL_15478, "Build mode");
        }
    }

    private boolean inBuildModeHouse() {
        return client.getVarbitValue(IN_HOUSE_BUILD_MODE_VARBIT) > 0; // == 1
    }

    private boolean isTalkingToPhials() {
        Widget chatTitleParent = client.getWidget(WidgetInfoExtended.DIALOG_OPTION_OPTION1.getId());

        if (chatTitleParent != null && !chatTitleParent.isHidden())
        {
            Widget chatTitleChild = chatTitleParent.getChild(3);
            return chatTitleChild.getText().contains("Exchange All");
        }
        return false;
    }

    private boolean hasNotedPlanks() {
        Product product = config.product();

        int notedPlankId = product.getPlankId() + 1; // TODO: is this always true!?
        // TODO: Search with text and use .onlyNoted();
        return Inventory.search().withId(notedPlankId).first().isPresent();
    }

    private boolean hasBuildInterface() {
        Product product = config.product();
        return Widgets.search().withId(BUILD_INTERFACE_WIDGET_ID).first().isPresent();
    }

    private boolean hasBuilt() {
        Product product = config.product();
        return TileObjects.search().withId(product.getRemoveSpaceId()).nearestToPlayer().isPresent();
    }

    private boolean hasTools() {
        boolean hasCoins = Inventory.search().nameContains("Coins").first().isPresent();
        boolean hasHammer = Inventory.search().nameContains("Hammer").first().isPresent();
        boolean hasSaw = Inventory.search().nameContains("Saw").first().isPresent();
        boolean hasTools = hasCoins && hasHammer && hasSaw;

        if (config.method() == Method.PHIALS) {
            return hasTools && hasNotedPlanks();
        }
        return hasTools;
    }

    private boolean hasEnoughUnnotedPlanks() {
        Product product = config.product();
        return Inventory.search().withId(product.getPlankId()).onlyUnnoted()
                .result().stream().count() >= product.getRequiredPlanks();
    }

    public void pressKey(char key)
    {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    private void keyEvent(int id, char key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );
        client.getCanvas().dispatchEvent(e);
    }
}
