package dextro.zeon.addon.modules.Chat;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SurroundAlert extends Module {

    public enum NotifMode {
        Client,
        Alert,
        Both
    }
    
    public SurroundAlert() {
        super(Zeon.Chat, "surround-alert", "Notifies u when player starting break ur surround!");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSBreak = settings.createGroup("Surround Break");

    private final Setting<NotifMode> notificationMode = sgGeneral.add(new EnumSetting.Builder<NotifMode>()
            .name("mode")
            .description("The mode to use for notifications.")
            .defaultValue(NotifMode.Alert)
            .build()
        );
    
    private final Setting<Boolean> surroundBreak = sgSBreak.add(new BoolSetting.Builder()
            .name("surround-break")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> head = sgSBreak.add(new BoolSetting.Builder()
            .name("head-break")
            .defaultValue(true)
            .visible(surroundBreak::get)
            .build()
    );

    private final Setting<Boolean> face = sgSBreak.add(new BoolSetting.Builder()
            .name("face-break")
            .defaultValue(true)
            .visible(surroundBreak::get)
            .build()
    );

    private final Setting<Boolean> legs = sgSBreak.add(new BoolSetting.Builder()
            .name("legs-break")
            .defaultValue(true)
            .visible(surroundBreak::get)
            .build()
    );

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    private long lastTick = 0;
    Set<PlayerEntity> playersBur = new HashSet<PlayerEntity>();
    private String m = " breaking ";
    private int timer = 0;
    private int count;


    @Override
    public void onDeactivate() {
        toLookup.clear();
    }
    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

    @EventHandler
    private void a(PacketEvent.Receive event) {
        if(surroundBreak.get() == true) {
            if (event.packet instanceof BlockBreakingProgressS2CPacket) {
                BlockBreakingProgressS2CPacket w = (BlockBreakingProgressS2CPacket) event.packet;

                if(w.getProgress() != 0) return;
                String player = mc.world.getEntityById(w.getEntityId()).getName().asString();
                BlockPos p = mc.player.getBlockPos();
                BlockPos brpos = w.getPos();
                if(legs.get()) {
                	for(BlockPos a : new BlockPos[] {p.east(), p.west(), p.south(), p.north()}) {
                
                	if(a.equals(brpos)) {
                		if(notificationMode.get() == NotifMode.Client)
                		{
                		info(player+m+"legs");
                		} 
                		else if(notificationMode.get() == NotifMode.Alert)
                		{
                			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "legs"));
                		    count++;
                		}
                		else if(notificationMode.get() == NotifMode.Both)
                		{
                			info(player+m+"legs");
                			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "legs"));
                			count++;
                		}
                	  }
                	}
                }
              
           
                if(face.get()) 
                {
                	for(BlockPos a : new BlockPos[] {p.up().east(), p.up().west(), p.up().south(), p.up().north()}) 
                	{
                		if(a.equals(brpos)) {
                    		if(notificationMode.get() == NotifMode.Client)
                    		{
                    		info(player+m+"face");
                    		} 
                    		else if(notificationMode.get() == NotifMode.Alert)
                    		{
                    			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "face"));
                    			count++;
                    		}
                    		else if(notificationMode.get() == NotifMode.Both)
                    		{
                    			info(player+m+"face");
                    			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "face"));
                    			count++;
                    		}
                    	  }
                    }
                }
                if(head.get() && p.up(2).equals(brpos)) 
                {
                	if(notificationMode.get() == NotifMode.Client)
            		{
            		info(player+m+"head");
            		} 
            		else if(notificationMode.get() == NotifMode.Alert)
            		{
            			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "head"));
            		}
            		else if(notificationMode.get() == NotifMode.Both)
            		{
            			info(player+m+"head");
            			mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, title, player + m + "head"));
            		}
                }
            }
        }
      
          }
        
    


    public BaseText formatMessage(String message, Vec3d coords) {
        BaseText text = new LiteralText(message);
        text.append(ChatUtils.formatCoords(coords));
        text.append(Formatting.GRAY.toString()+".");
        return text;
    }

    public BaseText formatMessage(String message, BlockPos coords) {
        return formatMessage(message, new Vec3d(coords.getX(), coords.getY(), coords.getZ()));
    }
}