package com.impact.NeverLog;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Client;

import javax.inject.Inject;
import java.util.Random;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;

import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#fa5555\">Never Log</font></html>",
        description = "Never log out, helps with other scripts",
        enabledByDefault = false,
        tags = {"ethan"}
)
public class NeverLogPlugin extends Plugin {
    @Inject
    private Client client;

    private final Random random = new Random();
    private long randomDelay;

    @Override
    protected void startUp()
    {
        randomDelay = randomDelay();
    }

    @Override
    protected void shutDown()
    {
        randomDelay = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (checkIdleLogout())
        {
            randomDelay = randomDelay();
            Executors.newSingleThreadExecutor()
                    .submit(this::pressKey);
        }
    }

    private boolean checkIdleLogout()
    {
        int idleClientTicks = client.getKeyboardIdleTicks();

        if (client.getMouseIdleTicks() < idleClientTicks)
        {
            idleClientTicks = client.getMouseIdleTicks();
        }

        return idleClientTicks >= randomDelay;
    }

    private long randomDelay()
    {
        return (long) clamp(
                Math.round(random.nextGaussian() * 8000)
        );
    }

    private static double clamp(double val)
    {
        return Math.max(1, Math.min(13000, val));
    }

    private void pressKey()
    {
        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), BUTTON1_DOWN_MASK, KeyEvent.VK_BACK_SPACE);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE);
        this.client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE);
        this.client.getCanvas().dispatchEvent(keyTyped);
    }

}
