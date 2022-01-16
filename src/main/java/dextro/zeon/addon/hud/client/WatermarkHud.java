package dextro.zeon.addon.hud.client;


import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class WatermarkHud extends DoubleTextHudElement {
    public WatermarkHud(HUD hud) {
        super(hud, "ZEON-watermark", "Display ZEON Watermark!.", "");
    }

    @Override
    protected String getRight() {
        return "ZEON " + Zeon.VERSION; }
}