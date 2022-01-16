package dextro.zeon.addon.mixins;

import meteordevelopment.meteorclient.systems.modules.render.hud.modules.HudElement;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.WatermarkHud;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.WelcomeHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = meteordevelopment.meteorclient.systems.modules.render.hud.HudElementLayer.class, remap = false)
    public class REMOVE_HUDS {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void add(HudElement element, CallbackInfo info) {
        if (element instanceof WelcomeHud || element instanceof WatermarkHud) info.cancel();
    }
}