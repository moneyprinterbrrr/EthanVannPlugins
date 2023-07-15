package com.impact.NightmareZone;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;

public abstract class Task
{
    public Task(NightmareZonePlugin plugin, Client client, NightmareZoneConfig config)
    {
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Inject
    public NightmareZonePlugin plugin;

    @Inject
    public Client client;

    @Inject
    public NightmareZoneConfig config;

    public MenuEntry entry;

    public abstract boolean validate();

    public String getTaskDescription()
    {
        return this.getClass().getSimpleName();
    }

    public void onGameTick(GameTick event) { }
}