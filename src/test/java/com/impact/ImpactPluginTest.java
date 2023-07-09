package com.impact;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.impact.PowerGather.PowerGatherPlugin;
import com.impact.NeverLog.NeverLogPlugin;
import com.impact.Alchemy.AlchemyPlugin;
import com.impact.Thiever.ThieverPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

// TODO: `Main` in impact plugin directory
// TODO: jar builder

public class ImpactPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(
                EthanApiPlugin.class, PacketUtilsPlugin.class, PowerGatherPlugin.class,
                NeverLogPlugin.class, AlchemyPlugin.class, ThieverPlugin.class
        );
        RuneLite.main(args);
    }
}