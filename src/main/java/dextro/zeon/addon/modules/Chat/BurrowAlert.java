package dextro.zeon.addon.modules.Chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BurrowAlert extends Module {

    public enum NotifMode {
        Client,
        Alert,
        Both
    }
    
    public BurrowAlert() {
        super(Zeon.Chat, "burrow-alert", "Notifies u when player using burrow!");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBurrow = settings.createGroup("Burrow");

    private final Setting<NotifMode> notificationMode = sgGeneral.add(new EnumSetting.Builder<NotifMode>()
            .name("mode")
            .description("The mode to use for notifications.")
            .defaultValue(NotifMode.Alert)
            .build()
        );
    
    private final Setting<Boolean> renderBur = sgBurrow.add(new BoolSetting.Builder()
            .name("render-burrow")
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgBurrow.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(renderBur::get)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgBurrow.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color of the target block rendering.")
            .defaultValue(new SettingColor(197, 137, 232, 10))
            .visible(renderBur::get)
            .build()
    );

    private final Setting<SettingColor> lineColor = sgBurrow.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color of the target block rendering.")
            .defaultValue(new SettingColor(197, 137, 232))
            .visible(renderBur::get)
            .build()
    );

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    Set<PlayerEntity> playersBur = new HashSet<PlayerEntity>();

    @Override
    public void onActivate() {
       

        	playersBur.clear(); 

    }

    @Override
    public void onDeactivate() {
        toLookup.clear();
    }
    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

        
    

    @EventHandler
    public void onTick(TickEvent.Post event) {
            if (mc.world == null || mc.player == null) return;

            for(PlayerEntity p : new ArrayList<PlayerEntity>(playersBur)) if(!mc.world.getPlayers().contains(p) || !inBlock(p) ) playersBur.remove(p);

            for(PlayerEntity p : mc.world.getPlayers()){
                if(p.equals(mc.player)) continue;
                if(playersBur.contains(p)) continue;
                if(!p.isOnGround()) continue;
                if(!Friends.get().shouldAttack(p)) continue;
                if(inBlock(p)){
                	if(notificationMode.get() == NotifMode.Client)
            		{
                    BaseText t = new LiteralText("Player ");

                    BaseText t2 = (BaseText) p.getName();
                    t2.setStyle(t2.getStyle().withColor(TextColor.fromRgb(16711680)));

                    t.append(t2);
                    t.append(" burrowed in ");
                    t.append(new TranslatableText(mc.world.getBlockState(p.getBlockPos()).getBlock().getTranslationKey()) );
                    info(t);
                    playersBur.add(p);
            		} 
                	else if(notificationMode.get() == NotifMode.Alert)
            		{
                		mc.getToastManager().add(new MeteorToast(Items.ENDER_CHEST, title, p.getName() + " burrowed in " + mc.world.getBlockState(p.getBlockPos()).getBlock().getTranslationKey()));
                		playersBur.add(p);
            		}
                	else if(notificationMode.get() == NotifMode.Both)
            		{
                		BaseText t = new LiteralText("Player ");

                        BaseText t2 = (BaseText) p.getName();
                        t2.setStyle(t2.getStyle().withColor(TextColor.fromRgb(16711680)));

                        t.append(t2);
                        t.append(" burrowed in ");
                        t.append(mc.world.getBlockState(p.getBlockPos()).getBlock().toString()) ;
                        info(t);
                        mc.getToastManager().add(new MeteorToast(Items.ENDER_CHEST, title, p.getName() + " burrowed in " + mc.world.getBlockState(p.getBlockPos()).getBlock().getTranslationKey()));
                        playersBur.add(p);
            		}
                }
            }
        }
    

    @EventHandler
    private void RenderEvent(Render3DEvent e) {
        if(renderBur.get()) for(PlayerEntity p : playersBur) if(mc.world.getPlayers().contains(p)) e.renderer.box( p.getBlockPos(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }


    private boolean inBlock(PlayerEntity p){
        return ( mc.world.getBlockState(p.getBlockPos()).isFullCube(mc.world, p.getBlockPos())
                || ( mc.world.getBlockState(p.getBlockPos()).hasBlockEntity() && mc.world.getBlockState(p.getBlockPos()).getHardness(mc.world, p.getBlockPos()) > 10 )
                || mc.world.getBlockState(p.getBlockPos()).getBlock() instanceof AnvilBlock
        ) ? true : false;
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

    private String percent(EquipmentSlot slot){
        ItemStack s = mc.player.getEquippedStack(slot);
        return Math.round(((s.getMaxDamage() - s.getDamage()) * 100f) / s.getMaxDamage()) + "%";
    }

    private TextColor color(EquipmentSlot slot){
        int current = mc.player.getEquippedStack(slot).getDamage();
        int max = mc.player.getEquippedStack(slot).getMaxDamage();
        int r = 255 - Math.round(((max - current) * 255) / max);
        int g = Math.round(((max - current) * 255) / max);
        return TextColor.fromRgb( ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(0&0x0ff) );
    }
}