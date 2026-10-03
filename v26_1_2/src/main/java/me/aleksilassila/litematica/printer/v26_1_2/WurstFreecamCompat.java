package me.aleksilassila.litematica.printer.v26_1_2;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class WurstFreecamCompat {
    private static boolean initialized;
    private static Field instance;
    private static Method getHax;
    private static Field freecam;
    private static Method isEnabled;

    private WurstFreecamCompat() {}

    static boolean isActive() {
        if (!initialized) {
            initialized = true;
            if (!FabricLoader.getInstance().isModLoaded("wurst")) return false;
            try {
                // Resolve only Wurst's public API; do not require it at compile time.
                Class<?> client = Class.forName("net.wurstclient.WurstClient", false,
                        WurstFreecamCompat.class.getClassLoader());
                instance = client.getField("INSTANCE");
                getHax = client.getMethod("getHax");
                freecam = getHax.getReturnType().getField("freecamHack");
                isEnabled = freecam.getType().getMethod("isEnabled");
            } catch (ReflectiveOperationException | LinkageError e) {
                disable(e);
            }
        }
        if (isEnabled == null) return false;
        try {
            Object client = instance.get(null);
            if (client == null) return false;
            Object hacks = getHax.invoke(client);
            if (hacks == null) return false;
            Object hack = freecam.get(hacks);
            return hack != null && (boolean) isEnabled.invoke(hack);
        } catch (ReflectiveOperationException | LinkageError e) {
            disable(e);
            return false;
        }
    }

    private static void disable(Throwable error) {
        isEnabled = null;
        org.apache.logging.log4j.LogManager.getLogger("litematica-printer")
                .warn("Could not detect Wurst Freecam; disabling Wurst compatibility check", error);
    }
}
