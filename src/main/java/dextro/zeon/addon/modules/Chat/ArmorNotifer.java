package dextro.zeon.addon.modules.Chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ArmorNotifer extends Module {
    
    public ArmorNotifer() {
        super(Zeon.Chat, "armor-notifer", "Show the armor strength when did u get damaged!");
    }
    
    private final SettingGroup sgABreak = settings.createGroup("Armor Break");


    private final Setting<Integer> seconds = sgABreak.add(new IntSetting.Builder()
            .name("show-seconds")
            .defaultValue(10)
            .min(1)
            .sliderMin(1).sliderMax(500)
            .build());

    private final Setting<String> separator = sgABreak.add(new StringSetting.Builder()
            .name("separator")
            .defaultValue("&7 | ")
            .build());

    private final Setting<Boolean> helmet = sgABreak.add(new BoolSetting.Builder()
            .name("helmet")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> chest = sgABreak.add(new BoolSetting.Builder()
            .name("chest")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> leegs = sgABreak.add(new BoolSetting.Builder()
            .name("legs")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> feet = sgABreak.add(new BoolSetting.Builder()
            .name("feet")
            .defaultValue(true)
            .build()
    );

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    Set<PlayerEntity> playersBur = new HashSet<PlayerEntity>();
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
    private void PacketEvent(PacketEvent.Receive e) {
            if (e.packet instanceof EntityStatusS2CPacket && ((EntityStatusS2CPacket) e.packet).getStatus() == 2 && ((EntityStatusS2CPacket) e.packet).getEntity(mc.world) == mc.player) timer = seconds.get() * 20;
    }

    @EventHandler
    private void ag(TickEvent.Pre e){
    	if(count >= 4) count = 0;
        if(timer > 0){
            timer--;

            BaseText text = new LiteralText("");

            List<BaseText> list = new ArrayList<>();

            if (helmet.get() && !mc.player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                BaseText a = new LiteralText("§lＨＥＡＤ§r － " + percent(EquipmentSlot.HEAD));
                a.setStyle(a.getStyle().withColor(color(EquipmentSlot.HEAD)));
                list.add(a);
            }
            if (chest.get() && !mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
                BaseText a = new LiteralText(String.format("§l%s§r － %s", mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) ? "ＥＬＹＴＲＡ" : "ＣＨＥＳＴ", percent(EquipmentSlot.CHEST)));
                a.setStyle(a.getStyle().withColor(color(EquipmentSlot.CHEST)));
                list.add(a);
            }
            if (leegs.get() && !mc.player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) {
                BaseText a = new LiteralText("§lＬＥＧＳ§r － " + percent(EquipmentSlot.LEGS));
                a.setStyle(a.getStyle().withColor(color(EquipmentSlot.LEGS)));
                list.add(a);
            }
            if (feet.get() && !mc.player.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
                BaseText a = new LiteralText("§lＦＥＥＴ§r － " + percent(EquipmentSlot.FEET));
                a.setStyle(a.getStyle().withColor(color(EquipmentSlot.FEET)));
                list.add(a);
            }
            for(int a = 0; a < list.size(); a++) {
                text.append(list.get(a));
                if (a < list.size() - 1) text.append( new LiteralText(separator.get().replace("&", "§")) );
            }

            mc.inGameHud.setOverlayMessage(text, true);
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