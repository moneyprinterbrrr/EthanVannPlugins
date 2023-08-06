package com.impact.NightmareZone;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.ui.overlay.Overlay;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class NightmareZoneOverlay extends Overlay
{
    private final Client client;
    private final NightmareZonePlugin plugin;
    private final NightmareZoneConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private NightmareZoneOverlay(Client client, NightmareZonePlugin plugin, NightmareZoneConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        this.setPriority(OverlayPriority.HIGHEST);
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Nightmare Zone Overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.disablePaint())
            return null;

        if (plugin == null)
            return null;

        if (!plugin.started)
            return null;

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Impact Nightmare Zone")
                .color(Color.decode("#fa5555"))
                .build());

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Status: " + plugin.status)
                .color(Color.ORANGE)
                .build());

        panelComponent.setPreferredSize(new Dimension(175, 100));
        panelComponent.setBackgroundColor(Color.BLACK);

        return panelComponent.render(graphics);
    }
}