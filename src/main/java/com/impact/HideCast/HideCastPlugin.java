package com.impact.HideCast;

import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.List;

@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Hide Cast</font></html>",
        description = "Removes cast menu options for clan members",
        tags = {"pvp"}
)
public class HideCastPlugin extends Plugin {

    @Inject
    private Client client;

    private static final List<MenuAction> PLAYER_MENU_TYPES = List.of(
			//MenuAction.WALK,
            MenuAction.WIDGET_TARGET_ON_PLAYER,
			//MenuAction.ITEM_USE_ON_PLAYER,
            MenuAction.PLAYER_FIRST_OPTION,
            MenuAction.PLAYER_SECOND_OPTION,
            MenuAction.PLAYER_THIRD_OPTION,
            MenuAction.PLAYER_FOURTH_OPTION,
            MenuAction.PLAYER_FIFTH_OPTION,
            MenuAction.PLAYER_SIXTH_OPTION,
            MenuAction.PLAYER_SEVENTH_OPTION,
            MenuAction.PLAYER_EIGTH_OPTION
			//MenuAction.RUNELITE_PLAYER
    );

    @Subscribe
    public void onClientTick(ClientTick clientTick)
    {
        // The menu is not rebuilt when it is open, so don't swap or else it will
        // repeatedly swap entries
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
        {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();

        int idx = 0;
        for (MenuEntry entry : menuEntries)
        {
            findCastAndDelete(menuEntries, idx++, entry);
        }
    }

    private void findCastAndDelete(MenuEntry[] menuEntries, int index, MenuEntry menuEntry)
    {
        final MenuAction menuAction = menuEntry.getType();
        final String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
        if (PLAYER_MENU_TYPES.contains(menuAction))
        {
            final Player player = menuEntry.getPlayer();
            assert player != null;
            final boolean isAttack = option.contains("Attack".toLowerCase());
            final boolean isCast = option.contains("Cast".toLowerCase());

            if ((isAttack || isCast) && player.isFriendsChatMember())
            {
                client.setMenuEntries(ArrayUtils.remove(menuEntries, index));
            }
        }
    }

}
