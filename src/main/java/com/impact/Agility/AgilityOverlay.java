package com.impact.Agility;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class AgilityOverlay extends OverlayPanel {

    private final AgilityPlugin plugin;
    @Inject
    private AgilityOverlay(AgilityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPreferredSize(new Dimension(200, 160));
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Impact Agility")
                .color(Color.decode("#fa5555"))
                .build());

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(plugin.isStartAgility() ? "Running" : "Paused")
                .color(plugin.isStartAgility() ? Color.GREEN : Color.RED)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Elapsed Time: ")
                .leftColor(Color.ORANGE)
                .right(plugin.getElapsedTime())
                .rightColor(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Course: ")
                .leftColor(Color.ORANGE)
                .right(plugin.getCourseName())
                .rightColor(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("State: ")
                .leftColor(Color.ORANGE)
                .right(plugin.getState() != null ? plugin.getState().name() : "null")
                .rightColor(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Marks collected: ")
                .leftColor(Color.ORANGE)
                .right(String.valueOf(plugin.getMogCollectCount()))
                .rightColor(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Marks/hr: ")
                .leftColor(Color.ORANGE)
                .right(String.valueOf(plugin.getMarksPH()))
                .rightColor(Color.WHITE)
                .build());
        return super.render(graphics);
    }
}