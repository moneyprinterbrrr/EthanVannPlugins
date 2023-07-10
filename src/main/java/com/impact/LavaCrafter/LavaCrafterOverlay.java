package com.impact.LavaCrafter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.ui.overlay.Overlay;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class LavaCrafterOverlay extends Overlay {
    private Client client;
    private LavaCrafterPlugin plugin;
    private LavaCrafterConfig config;
    private PanelComponent panelComponent = new PanelComponent();

    @Inject
    private LavaCrafterOverlay(Client client, LavaCrafterPlugin plugin, LavaCrafterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        this.setPriority(OverlayPriority.HIGHEST);
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Lava Crafter Overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.disablePaint()) {
            return null;
        }

        if (plugin == null)
            return null;

        if (!plugin.started)
            return null;

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Impact Lava Crafter")
                .color(Color.PINK)
                .build());

        if (plugin.watch != null && plugin.watch.isStarted()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Timer: " + plugin.watch.toString())
                    .color(Color.ORANGE)
                    .build());
        }

        if (plugin.state != null)
        {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("State: " + plugin.state.name())
                    .color(Color.ORANGE)
                    .build());
        }

        panelComponent.setPreferredSize(new Dimension(175, 100));
        panelComponent.setBackgroundColor(Color.BLACK);

        return panelComponent.render(graphics);
    }
}