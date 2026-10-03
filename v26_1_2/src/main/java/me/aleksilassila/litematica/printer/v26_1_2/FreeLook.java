package me.aleksilassila.litematica.printer.v26_1_2;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import me.aleksilassila.litematica.printer.v26_1_2.config.PrinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;

public class FreeLook {
    static FreeLook INSTANCE = null;
    float cameraYaw = 0;
    float cameraPitch = 0;
    CameraType prevPerspective = CameraType.FIRST_PERSON;
    Minecraft mc = Minecraft.getInstance();
    boolean enabled = false;
    int ticksSinceLastRotation = 0;

//    BooleanSetting changePers = addBooleanSetting("Change CameraType",true);
//    EnumSetting<Mode> cameraMode = addEnumSetting("Camera Mode",Mode.CAMERA);
//    FloatSetting sensitivity = addFloatSetting("Sensitivity",8,0,10);
    private FreeLook() {
        enabled = PrinterConfig.FREE_LOOK.getBooleanValue();
        PrinterConfig.FREE_LOOK.setValueChangeCallback(this::setEnabled);
    }

    public static FreeLook getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FreeLook();
        }
        return INSTANCE;
    }

    public void onGameTick() {
        if (shouldRotate() && ((ticksSinceLastRotation -1) == PrinterConfig.FREE_LOOK_LOOK_BACK.getIntegerValue() || PrinterConfig.FREE_LOOK_LOOK_BACK_ALWAYS_ROTATE_PLAYER.getBooleanValue())) {
            if (mc.player != null) {
                // Reset player rotation. The mouse mixin only rotates the player by a delta. So without this the player
                // rotation would be offset from the camera rotation.
                mc.player.setYRot(cameraYaw);
                mc.player.setXRot(cameraPitch);
            }
        }
        ticksSinceLastRotation++;
    }

    private void setEnabled(ConfigBoolean configBoolean) {
        this.enabled = configBoolean.getBooleanValue();
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) System.out.println("FreeLook: " + enabled);
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    void onEnable() {
        if(mc.player == null) {
            return;
        }
        this.enabled = true;
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) System.out.println("FreeLook: onEnable");

        cameraPitch = mc.player.getXRot();
        cameraYaw = mc.player.getYRot();
        prevPerspective = mc.options.getCameraType();

        if (PrinterConfig.FREE_LOOK_THIRD_PERSON.getBooleanValue()) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    void onDisable() {
        this.enabled = false;
        if (PrinterConfig.PRINTER_DEBUG_LOG.getBooleanValue()) System.out.println("FreeLook: onDisable");
        if (prevPerspective != null && mc.options.getCameraType() != prevPerspective /*&& changePers.get()*/) mc.options.setCameraType(prevPerspective);
    }

    /**
     * True when another mod has taken the camera off the player, as a freecam does.
     */
    public boolean isExternalCameraActive() {
        Entity camera = mc.getCameraEntity();
        return (camera != null && camera != mc.player) || WurstFreecamCompat.isActive();
    }

    public boolean isEnabled() {
        // Stand down while another mod owns the camera, so its freecam keeps control of both
        // the view and what the mouse rotates. Printing still rotates the player itself:
        // Printer#rotate does not consult free look.
        return enabled && !isExternalCameraActive();
    }

    public boolean shouldRotate() {
        return isEnabled() && PrinterConfig.FREE_LOOK_LOOK_BACK.getIntegerValue() != 0 && ticksSinceLastRotation > PrinterConfig.FREE_LOOK_LOOK_BACK.getIntegerValue();
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public void setCameraYaw(float v) {
        cameraYaw = v;
    }

    public void setCameraPitch(float v) {
        cameraPitch = v;
    }

    public void setPrevPerspective(CameraType perspective) {
        prevPerspective = perspective;
    }

    public CameraType getPrevPerspective() {
        return prevPerspective;
    }
}
