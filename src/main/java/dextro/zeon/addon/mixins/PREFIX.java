package dextro.zeon.addon.mixins;

import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = meteordevelopment.meteorclient.utils.player.ChatUtils.class, remap = false)
    public class PREFIX {
    @Inject(method = "getPrefix", at = @At("HEAD"), cancellable = true)
    private static void getPrefix(CallbackInfoReturnable<Text> info) {
        BaseText text = new LiteralText("§7[§9§lZEON§7] ");
        info.setReturnValue(text);
    }
}
