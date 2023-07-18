package com.impact.ReorderPrayers;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.example.PacketUtils.WidgetInfoExtended;
import com.example.PacketUtils.WidgetID;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetConfig.DRAG;
import static net.runelite.api.widgets.WidgetConfig.DRAG_ON;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Reorder Prayers</font></html>",
        description = "Reorder the prayers displayed on the Prayer panel",
        tags = {"pvp"}
)
public class ReorderPrayersPlugin extends Plugin
{

    static final String CONFIG_GROUP_KEY = "ReorderPrayers";

    static final String CONFIG_UNLOCK_REORDERING_KEY = "unlockPrayerReordering";

    static final String CONFIG_PRAYER_ORDER_KEY = "prayerOrder";

    private static final int PRAYER_WIDTH = 34;

    private static final int PRAYER_HEIGHT = 34;

    private static final int PRAYER_X_OFFSET = 37;

    private static final int PRAYER_Y_OFFSET = 37;

    private static final int QUICK_PRAYER_SPRITE_X_OFFSET = 2;

    private static final int QUICK_PRAYER_SPRITE_Y_OFFSET = 2;

    private static final int PRAYER_COLUMN_COUNT = 5;

    private static final int PRAYER_COUNT = Prayer.values().length;

    private static final List<WidgetInfoExtended> PRAYER_WIDGET_INFO_LIST = ImmutableList.of(
            WidgetInfoExtended.PRAYER_THICK_SKIN,
            WidgetInfoExtended.PRAYER_BURST_OF_STRENGTH,
            WidgetInfoExtended.PRAYER_CLARITY_OF_THOUGHT,
            WidgetInfoExtended.PRAYER_SHARP_EYE,
            WidgetInfoExtended.PRAYER_MYSTIC_WILL,
            WidgetInfoExtended.PRAYER_ROCK_SKIN,
            WidgetInfoExtended.PRAYER_SUPERHUMAN_STRENGTH,
            WidgetInfoExtended.PRAYER_IMPROVED_REFLEXES,
            WidgetInfoExtended.PRAYER_RAPID_RESTORE,
            WidgetInfoExtended.PRAYER_RAPID_HEAL,
            WidgetInfoExtended.PRAYER_PROTECT_ITEM,
            WidgetInfoExtended.PRAYER_HAWK_EYE,
            WidgetInfoExtended.PRAYER_MYSTIC_LORE,
            WidgetInfoExtended.PRAYER_STEEL_SKIN,
            WidgetInfoExtended.PRAYER_ULTIMATE_STRENGTH,
            WidgetInfoExtended.PRAYER_INCREDIBLE_REFLEXES,
            WidgetInfoExtended.PRAYER_PROTECT_FROM_MAGIC,
            WidgetInfoExtended.PRAYER_PROTECT_FROM_MISSILES,
            WidgetInfoExtended.PRAYER_PROTECT_FROM_MELEE,
            WidgetInfoExtended.PRAYER_EAGLE_EYE,
            WidgetInfoExtended.PRAYER_MYSTIC_MIGHT,
            WidgetInfoExtended.PRAYER_RETRIBUTION,
            WidgetInfoExtended.PRAYER_REDEMPTION,
            WidgetInfoExtended.PRAYER_SMITE,
            WidgetInfoExtended.PRAYER_PRESERVE,
            WidgetInfoExtended.PRAYER_CHIVALRY,
            WidgetInfoExtended.PRAYER_PIETY,
            WidgetInfoExtended.PRAYER_RIGOUR,
            WidgetInfoExtended.PRAYER_AUGURY
    );

    private static final List<Integer> QUICK_PRAYER_CHILD_IDS = ImmutableList.of(
            WidgetID.QuickPrayer.THICK_SKIN_CHILD_ID,
            WidgetID.QuickPrayer.BURST_OF_STRENGTH_CHILD_ID,
            WidgetID.QuickPrayer.CLARITY_OF_THOUGHT_CHILD_ID,
            WidgetID.QuickPrayer.SHARP_EYE_CHILD_ID,
            WidgetID.QuickPrayer.MYSTIC_WILL_CHILD_ID,
            WidgetID.QuickPrayer.ROCK_SKIN_CHILD_ID,
            WidgetID.QuickPrayer.SUPERHUMAN_STRENGTH_CHILD_ID,
            WidgetID.QuickPrayer.IMPROVED_REFLEXES_CHILD_ID,
            WidgetID.QuickPrayer.RAPID_RESTORE_CHILD_ID,
            WidgetID.QuickPrayer.RAPID_HEAL_CHILD_ID,
            WidgetID.QuickPrayer.PROTECT_ITEM_CHILD_ID,
            WidgetID.QuickPrayer.HAWK_EYE_CHILD_ID,
            WidgetID.QuickPrayer.MYSTIC_LORE_CHILD_ID,
            WidgetID.QuickPrayer.STEEL_SKIN_CHILD_ID,
            WidgetID.QuickPrayer.ULTIMATE_STRENGTH_CHILD_ID,
            WidgetID.QuickPrayer.INCREDIBLE_REFLEXES_CHILD_ID,
            WidgetID.QuickPrayer.PROTECT_FROM_MAGIC_CHILD_ID,
            WidgetID.QuickPrayer.PROTECT_FROM_MISSILES_CHILD_ID,
            WidgetID.QuickPrayer.PROTECT_FROM_MELEE_CHILD_ID,
            WidgetID.QuickPrayer.EAGLE_EYE_CHILD_ID,
            WidgetID.QuickPrayer.MYSTIC_MIGHT_CHILD_ID,
            WidgetID.QuickPrayer.RETRIBUTION_CHILD_ID,
            WidgetID.QuickPrayer.REDEMPTION_CHILD_ID,
            WidgetID.QuickPrayer.SMITE_CHILD_ID,
            WidgetID.QuickPrayer.PRESERVE_CHILD_ID,
            WidgetID.QuickPrayer.CHIVALRY_CHILD_ID,
            WidgetID.QuickPrayer.PIETY_CHILD_ID,
            WidgetID.QuickPrayer.RIGOUR_CHILD_ID,
            WidgetID.QuickPrayer.AUGURY_CHILD_ID
    );

    public static final int NEW_PRAYER_WIDGET_SCRIPT_ID = 359;

    @Inject
    private Client client;

    @Inject
    private ReorderPrayersConfig config;

    @Inject
    private MenuManager menuManager;

    private Prayer[] prayerOrder;

    static String prayerOrderToString(Prayer[] prayerOrder)
    {
        return Arrays.stream(prayerOrder)
                .map(Prayer::name)
                .collect(Collectors.joining(","));
    }

    private static Prayer[] stringToPrayerOrder(String string)
    {
        return Arrays.stream(string.split(","))
                .map(Prayer::valueOf)
                .toArray(Prayer[]::new);
    }

    private static int getPrayerIndex(Widget widget)
    {
        int x = widget.getOriginalX() / PRAYER_X_OFFSET;
        int y = widget.getOriginalY() / PRAYER_Y_OFFSET;
        return x + y * PRAYER_COLUMN_COUNT;
    }

    private static void setWidgetPosition(Widget widget, int x, int y)
    {
        widget.setRelativeX(x);
        widget.setRelativeY(y);
        widget.setOriginalX(x);
        widget.setOriginalY(y);
    }

    @Provides
    ReorderPrayersConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ReorderPrayersConfig.class);
    }

    @Override
    protected void startUp()
    {
        prayerOrder = stringToPrayerOrder(config.prayerOrder());
        reorderPrayers();
    }

    @Override
    protected void shutDown()
    {
        prayerOrder = Prayer.values();
        reorderPrayers(false);
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        if (event.getScriptId() == NEW_PRAYER_WIDGET_SCRIPT_ID)
        {
            reorderPrayers();
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            reorderPrayers();
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals(CONFIG_GROUP_KEY))
        {
            if (event.getKey().equals(CONFIG_PRAYER_ORDER_KEY))
            {
                prayerOrder = stringToPrayerOrder(config.prayerOrder());
            }
            reorderPrayers();
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetID.PRAYER_GROUP_ID || event.getGroupId() == WidgetID.QUICK_PRAYERS_GROUP_ID)
        {
            reorderPrayers();
        }
    }

    @Subscribe
    private void onDraggingWidgetChanged(DraggingWidgetChanged event)
    {
        // is dragging widget and mouse button released
        if (event.isDraggingWidget() && client.getMouseCurrentButton() == 0)
        {
            Widget draggedWidget = client.getDraggedWidget();
            Widget draggedOnWidget = client.getDraggedOnWidget();
            if (draggedWidget != null && draggedOnWidget != null)
            {
                int draggedGroupId = WidgetInfo.TO_GROUP(draggedWidget.getId());
                int draggedOnGroupId = WidgetInfo.TO_GROUP(draggedOnWidget.getId());
                if (draggedGroupId != WidgetID.PRAYER_GROUP_ID || draggedOnGroupId != WidgetID.PRAYER_GROUP_ID
                        || draggedOnWidget.getWidth() != PRAYER_WIDTH || draggedOnWidget.getHeight() != PRAYER_HEIGHT)
                {
                    return;
                }
                // reset dragged on widget to prevent sending a drag widget packet to the server
                client.setDraggedOnWidget(null);

                int fromPrayerIndex = getPrayerIndex(draggedWidget);
                int toPrayerIndex = getPrayerIndex(draggedOnWidget);

                Prayer tmp = prayerOrder[toPrayerIndex];
                prayerOrder[toPrayerIndex] = prayerOrder[fromPrayerIndex];
                prayerOrder[fromPrayerIndex] = tmp;

                save();
            }
        }
    }

    private PrayerTabState getPrayerTabState()
    {
        HashTable<WidgetNode> componentTable = client.getComponentTable();
        for (WidgetNode widgetNode : componentTable)
        {
            if (widgetNode.getId() == WidgetID.PRAYER_GROUP_ID)
            {
                return PrayerTabState.PRAYERS;
            }
            else if (widgetNode.getId() == WidgetID.QUICK_PRAYERS_GROUP_ID)
            {
                return PrayerTabState.QUICK_PRAYERS;
            }
        }
        return PrayerTabState.NONE;
    }

    private void save()
    {
        config.prayerOrder(prayerOrderToString(prayerOrder));
    }

    private void reorderPrayers()
    {
        reorderPrayers(config.unlockPrayerReordering());
    }

    private void reorderPrayers(boolean unlocked)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        PrayerTabState prayerTabState = getPrayerTabState();

        if (prayerTabState == PrayerTabState.PRAYERS)
        {
            List<Widget> prayerWidgets = PRAYER_WIDGET_INFO_LIST.stream()
                    .map(w -> client.getWidget(w.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (prayerWidgets.size() != PRAYER_WIDGET_INFO_LIST.size())
            {
                return;
            }

            for (int index = 0; index < prayerOrder.length; index++)
            {
                Prayer prayer = prayerOrder[index];
                Widget prayerWidget = prayerWidgets.get(prayer.ordinal());

                int widgetConfig = prayerWidget.getClickMask();
                if (unlocked)
                {
                    // allow dragging of this widget
                    widgetConfig |= DRAG;
                    // allow this widget to be dragged on
                    widgetConfig |= DRAG_ON;
                }
                else
                {
                    // remove drag flag
                    widgetConfig &= ~DRAG;
                    // remove drag on flag
                    widgetConfig &= ~DRAG_ON;
                }
                prayerWidget.setClickMask(widgetConfig);

                int x = index % PRAYER_COLUMN_COUNT;
                int y = index / PRAYER_COLUMN_COUNT;
                int widgetX = x * PRAYER_X_OFFSET;
                int widgetY = y * PRAYER_Y_OFFSET;
                setWidgetPosition(prayerWidget, widgetX, widgetY);
            }
        }
        else if (prayerTabState == PrayerTabState.QUICK_PRAYERS)
        {
            Widget prayersContainer = client.getWidget(WidgetInfo.QUICK_PRAYER_PRAYERS);
            if (prayersContainer == null)
            {
                return;
            }
            Widget[] prayerWidgets = prayersContainer.getDynamicChildren();
            if (prayerWidgets == null || prayerWidgets.length != PRAYER_COUNT * 3)
            {
                return;
            }

            for (int index = 0; index < prayerOrder.length; index++)
            {
                Prayer prayer = prayerOrder[index];

                int x = index % PRAYER_COLUMN_COUNT;
                int y = index / PRAYER_COLUMN_COUNT;

                Widget prayerWidget = prayerWidgets[QUICK_PRAYER_CHILD_IDS.get(prayer.ordinal())];
                setWidgetPosition(prayerWidget, x * PRAYER_X_OFFSET, y * PRAYER_Y_OFFSET);

                int childId = PRAYER_COUNT + 2 * prayer.ordinal();

                Widget prayerSpriteWidget = prayerWidgets[childId];
                setWidgetPosition(prayerSpriteWidget,
                        QUICK_PRAYER_SPRITE_X_OFFSET + x * PRAYER_X_OFFSET,
                        QUICK_PRAYER_SPRITE_Y_OFFSET + y * PRAYER_Y_OFFSET);

                Widget prayerToggleWidget = prayerWidgets[childId + 1];
                setWidgetPosition(prayerToggleWidget, x * PRAYER_X_OFFSET, y * PRAYER_Y_OFFSET);
            }
        }
    }
}