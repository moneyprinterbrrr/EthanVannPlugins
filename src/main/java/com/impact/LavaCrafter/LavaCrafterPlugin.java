package com.impact.LavaCrafter;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Lava Crafter</font></html>",
        description = "Crafts lava runes, keep earth rune stack in inventory",
        tags = {"ethan", "skilling"}
)
public class LavaCrafterPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private KeyManager keyManager;

    @Inject
    private LavaCrafterConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LavaCrafterOverlay overlay;

    Random r = new Random();
    int nextRunVal = r.nextInt(99) + 1;
    private boolean hasFlickedRun = false;

    private MenuEntry entry;

    boolean started = false;
    private int tickDelay = 0;

    StopWatch watch = new StopWatch();

    LavaCrafterState state = LavaCrafterState.USE_BANK_CHEST;

    private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
            new ThreadPoolExecutor.DiscardPolicy());

    @Provides
    LavaCrafterConfig provideConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(LavaCrafterConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        started = false;
        state = LavaCrafterState.USE_BANK_CHEST;
        tickDelay = 0;
        watch.reset();
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.registerKeyListener(toggle);
        overlayManager.remove(overlay);
        stopPlugin();
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

        if (!started) {
            started = true;
            state = LavaCrafterState.USE_BANK_CHEST;
            tickDelay = 0;
            watch.reset();
            watch.start();
        } else {
            stopPlugin();
        }
    }

    public void stopPlugin()
    {
        started = false;
        state = LavaCrafterState.USE_BANK_CHEST;
        tickDelay = 0;
        executor.shutdownNow();
        watch.reset();
    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        ChatMessageType type = event.getType();
        String msg = event.getMessage();

        if (type == ChatMessageType.GAMEMESSAGE)
        {
            if (state == LavaCrafterState.TELE_PVP_ARENA || state == LavaCrafterState.TELE_CASTLE_WARS)
            {
                if (msg.contains("ring of dueling"))
                {
                    iterateState();
                    tickDelay = getRandomTickDelay(1);
                }
            }
            else if (state == LavaCrafterState.USE_EARTHS_ON_ALTAR)
            {
                if (msg.equals("You bind the temple's power into lava runes."))
                {
                    iterateState();
                    tickDelay = getRandomTickDelay(1);
                }
            }
        }
        else if (type == ChatMessageType.SPAM)
        {
            if (state == LavaCrafterState.ENTER_RUINS)
            {
                if (msg.equals("You feel a powerful force take hold of you..."))
                {
                    iterateState();
                    tickDelay = getRandomTickDelay(1);
                }
            }
            else if (state == LavaCrafterState.CAST_MAGIC_IMBUE)
            {
                if (msg.contains("You are charged to combine runes!"))
                {
                    iterateState();
                    tickDelay = getRandomTickDelay(1);
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!started)
            return;

        if (client.getGameState() == GameState.LOGIN_SCREEN)
        {
            stopPlugin();
            return;
        }

        if (config.useTimeStopCondition() && watch.getTime(TimeUnit.MINUTES) > config.timeStopConditionValue())
        {
            stopPlugin();
            return;
        }

        if (client.getLocalPlayer() == null)
            return;

        handleTaskCompletions();

        if (tickDelay > 0)
        {
            tickDelay--;
            return;
        }

        handleState();
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (config.useLevelStopCondition())
        {
            if (event.getSkill() != Skill.RUNECRAFT)
                return;

            if (event.getLevel() == config.levelStopConditionValue())
                stopPlugin();
        }
    }

    public void handleRun()
    {
        if (!config.autoEnableRun())
            return;

        boolean runEnabled = client.getVarpValue(173) == 1;
        int energy = client.getEnergy();

        if (!runEnabled && !hasFlickedRun && energy > nextRunVal * 100)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 10485787, -1, -1);
            nextRunVal = r.nextInt(99) + 1;
            hasFlickedRun = !hasFlickedRun;
        }
    }

    private void handleTaskCompletions()
    {
        switch (state)
        {
            case USE_BANK_CHEST:
                if (Bank.isOpen())
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case DEPOSIT_LAVAS:
                if (getInventoryItem(ItemID.LAVA_RUNE) == null)
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WEAR_BINDING_NECKLACE:
                if (hasBindingNecklace())
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WEAR_DUELING_RING:
                if (hasDuelingRing())
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WITHDRAW_DUELING_RING:
                if (getInventoryItem(ItemID.RING_OF_DUELING8, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING1) != null)
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WITHDRAW_TALISMAN:
                if (getInventoryItem(ItemID.EARTH_TALISMAN) != null)
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WITHDRAW_BINDING_NECKLACE:
                if (getInventoryItem(ItemID.BINDING_NECKLACE) != null)
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
            case WITHDRAW_ESSENCE:
                if (getInventoryItem(ItemID.PURE_ESSENCE) != null)
                {
                    tickDelay = getRandomTickDelay();
                    iterateState();
                }
                break;
        }
    }

    private void iterateState()
    {
        switch (state)
        {
            case USE_BANK_CHEST:
                state = LavaCrafterState.WITHDRAW_DUELING_RING;
                break;
            case WITHDRAW_DUELING_RING:
                state = LavaCrafterState.WEAR_DUELING_RING;
                break;
            case WEAR_DUELING_RING:
                if (!config.useBindingNecklace())
                    state = LavaCrafterState.DEPOSIT_LAVAS;
                else
                    state = LavaCrafterState.WITHDRAW_BINDING_NECKLACE;
                break;
            case WITHDRAW_BINDING_NECKLACE:
                state = LavaCrafterState.WEAR_BINDING_NECKLACE;
                break;
            case WEAR_BINDING_NECKLACE:
                state = LavaCrafterState.DEPOSIT_LAVAS;
                break;
            case DEPOSIT_LAVAS:
                if (config.useMagicImbue())
                    state = LavaCrafterState.WITHDRAW_ESSENCE;
                else
                    state = LavaCrafterState.WITHDRAW_TALISMAN;
                break;
            case WITHDRAW_TALISMAN:
                state = LavaCrafterState.WITHDRAW_ESSENCE;
                break;
            case WITHDRAW_ESSENCE:
                state = LavaCrafterState.TELE_PVP_ARENA;
                break;
            case TELE_PVP_ARENA:
                state = LavaCrafterState.ENTER_RUINS;
                break;
            case ENTER_RUINS:
                if (config.useMagicImbue())
                    state = LavaCrafterState.CAST_MAGIC_IMBUE;
                else
                    state = LavaCrafterState.USE_EARTHS_ON_ALTAR;
                break;
            case CAST_MAGIC_IMBUE:
                state = LavaCrafterState.USE_EARTHS_ON_ALTAR;
                break;
            case USE_EARTHS_ON_ALTAR:
                state = LavaCrafterState.TELE_CASTLE_WARS;
                break;
            case TELE_CASTLE_WARS:
                state = LavaCrafterState.USE_BANK_CHEST;
                break;
        }
    }

    private int getRandomTickDelay()
    {
        return r.nextInt(config.clickDelayMax() - config.clickDelayMin() + 1) + config.clickDelayMin();
    }

    private int getRandomTickDelay(int min)
    {
        int next = r.nextInt(config.clickDelayMax() - config.clickDelayMin() + 1) + config.clickDelayMin();

        //clamp to min
        if (next < min)
            next = min;

        return next;
    }

    public void handleState()
    {
        switch (state.type)
        {
            case GAME_OBJECT:
                if (client.getLocalPlayer() == null) // Think this is to make sure teleports go through
                {
                    return;
                }

                Optional<TileObject> object = TileObjects.search().withId(state.identifier).withinDistance(100).first();
                if (!object.isPresent())
                    return;

                switch (state)
                {
                    case USE_EARTHS_ON_ALTAR:
                        hasFlickedRun = false;
                        MousePackets.queueClickPacket();
                        ObjectPackets.queueWidgetOnTileObject(getInventoryItem(ItemID.EARTH_RUNE), object.get());
                        return;

                    default: // USE_BANK_CHEST, ENTER_RUINS
                        handleRun();
                        MousePackets.queueClickPacket();
                        TileObjectInteraction.interact(object.get(), state.option);
                        return;
                }

            case BANK_ITEM:
                List<Integer> bankItemIds = null;

                switch (state)
                {
                    case WITHDRAW_DUELING_RING:
                        if (hasDuelingRing())
                        {
                            //manually iterate state since it will be skipped in the main thread when we return null...
                            iterateState();
                            return;
                        }
                        bankItemIds = Arrays.asList(ItemID.RING_OF_DUELING8, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING1);
                        break;
                    case WITHDRAW_BINDING_NECKLACE:
                        if (hasBindingNecklace())
                        {
                            //manually iterate state since it will be skipped in the main thread when we return null...
                            iterateState();
                            return;
                        }
                        bankItemIds = Arrays.asList(ItemID.BINDING_NECKLACE);
                        break;
                    case WITHDRAW_TALISMAN:
                        bankItemIds = Arrays.asList(ItemID.EARTH_TALISMAN);
                        break;
                    case WITHDRAW_ESSENCE:
                        bankItemIds = Arrays.asList(ItemID.PURE_ESSENCE);
                        break;
                }

                Bank.search().idInList(bankItemIds).first()
                        .ifPresent(item -> BankInteraction.useItem(item, state.option));
                return;

            case BANK_INVENTORY_ITEM:

                Widget bankInventoryItem = null;

                switch (state)
                {
                    case DEPOSIT_LAVAS:
                        bankInventoryItem = getBankInventoryItem(ItemID.LAVA_RUNE);

                        if (bankInventoryItem == null)
                        {
                            this.state = LavaCrafterState.WITHDRAW_TALISMAN;
                        }

                        break;
                    case WEAR_BINDING_NECKLACE:
                        if (hasBindingNecklace())
                        {
                            //manually iterate state since it will be skipped in the main thread when we return null...
                            iterateState();
                            return;
                        }

                        bankInventoryItem = getBankInventoryItem(ItemID.BINDING_NECKLACE);

                        if (bankInventoryItem == null)
                        {
                            this.state = LavaCrafterState.WITHDRAW_BINDING_NECKLACE;
                        }

                        break;
                    case WEAR_DUELING_RING:
                        if (hasDuelingRing())
                        {
                            //manually iterate state since it will be skipped in the main thread when we return null...
                            iterateState();
                            return;
                        }

                        bankInventoryItem = getBankInventoryItem(ItemID.RING_OF_DUELING1, ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING7,ItemID.RING_OF_DUELING8);

                        if (bankInventoryItem == null)
                        {
                            this.state = LavaCrafterState.WITHDRAW_DUELING_RING;
                        }

                        break;
                }

                if (bankInventoryItem == null)
                    return;

                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(bankInventoryItem, state.option); //queueWidgetOnWidget(itemOne, itemTwo);
                return;

            default:
                switch (state) {
                    case CAST_MAGIC_IMBUE:
                        // TODO: not sure if this works
                        Optional<Widget> actionWidget = Widgets.search().withId(state.param1).first();
                        if (actionWidget.isPresent()) {
                            MousePackets.queueClickPacket();
                            WidgetPackets.queueWidgetAction(actionWidget.get(), state.option);
                        }
                        return;
                    default: // TELE_PVP_ARENA, TELE_CASTLE_WARS
                        Optional<EquipmentItemWidget> ringWidget = Equipment.search().nameContains("Ring of dueling(").first();
                        if (ringWidget.isPresent()) {
                            MousePackets.queueClickPacket();
                            WidgetPackets.queueWidgetAction(ringWidget.get(), state.option);
                        }
                        return;
                }
        }
    }

    public boolean hasBindingNecklace()
    {
        return Equipment.search().nameContains("Binding necklace")
                .result().size() > 0;
    }

    public boolean hasDuelingRing()
    {
        return Equipment.search().nameContains("Ring of dueling(")
                .result().size() > 0;
    }

    public Widget getInventoryItem(int... ids)
    {
        Optional<Widget> item = Inventory.search()
                .idInList(Arrays.stream(ids).boxed().collect(Collectors.toList()))
                .first();
        if (item.isPresent()) {
            return item.get();
        }
        return null;
    }

    public Widget getBankInventoryItem(int... ids)
    {
        Optional<Widget> item = BankInventory.search()
                .idInList(Arrays.stream(ids).boxed().collect(Collectors.toList()))
                .first();
        if (item.isPresent()) {
            return item.get();
        }
        return null;
    }
}
