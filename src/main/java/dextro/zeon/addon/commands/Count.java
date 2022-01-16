package dextro.zeon.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dextro.zeon.addon.utils.Ezz;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Count extends Command {
    public Count() {
        super("count", "Set item stack amount in main hand.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("count", IntegerArgumentType.integer(1, 127)).executes(context -> {
        	
            MinecraftClient mc = MinecraftClient.getInstance();
            
            if(mc.player.getMainHandStack().getItem() == Items.AIR) return 1;
            
            ItemStack stack = mc.player.getMainHandStack();
            stack.setCount(context.getArgument("count", Integer.class));
            int slot = Ezz.invIndexToSlotId(mc.player.getInventory().selectedSlot);
            
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, stack));
            
            mc.openScreen(new CreativeInventoryScreen(mc.player));
            
            return SINGLE_SUCCESS;
        }));
    }
}