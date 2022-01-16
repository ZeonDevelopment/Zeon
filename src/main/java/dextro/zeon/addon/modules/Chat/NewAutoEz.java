package dextro.zeon.addon.modules.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class NewAutoEz extends Module {
	
	public enum Mode {
		Client,
		Message
	}
	
	public NewAutoEz() {
		super(Zeon.Chat, "zeon-auto-ez", "Say eZZzzZ on every kills!");
	}
	boolean p = true;
	private final SettingGroup sgGeneral = settings.getDefaultGroup();
	
    private final Setting<Mode> b = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("Mode")
            .description("The mode.")
            .defaultValue(Mode.Message)
            .build()
    );
	
	private final Setting<Integer> minArmor = sgGeneral
			.add(new IntSetting.Builder()
			.name("min-armor")
			.description("Minimum number of armor elements.")
			.defaultValue(2)
			.min(0)
			.max(4)
			.sliderMin(0)
			.sliderMax(4)
			.build()
			);
	
	private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
			.name("ignore-friends")
			.defaultValue(true)
			.build());
	
	Setting<List<String>> killMessages = sgGeneral.add(new StringListSetting.Builder()
	        .name("messages")
	        .defaultValue(Arrays.asList(
	                "EzZZz {player} by ZEON for all! v0.2!",
	                "Join ZEON for free: https://discord.gg/YTQGdEEMBm",
	                "{player} just died by ZEON for all! v0.2!",
	                "{player} Ezzzzz by ZEON for all! v0.2!",
	                "I just EZZz'd {player} by ZEON for all v0.2!",
	                "Join ZEON for free: https://discord.gg/YTQGdEEMBm",
	                "I just fucked {player} by ZEON for all v0.2!",
	                "Killed {player} with ZEON for all v0.2!",
	                "Join ZEON for free: https://discord.gg/YTQGdEEMBm",
	                "Take the L nerd {player}! You just got ended by ZEON for all v0.2!",
	                "U got nae`d by ZEON for all v0.2!",
	                "Join ZEON for free: https://discord.gg/YTQGdEEMBm",
	                "Wow I didn't even use a totem. You suck, {player} by ZEON for all v0.2!",
	                "{player} died to ZEON for all v0.2!"
	            ))
	        .visible(() -> !p)
	            .build()
	        );
	
	
	@Override
	public void onActivate() {
		players.clear();
	}
	
	Map<Integer, Integer> players = new HashMap<>();
	
	private boolean checkArmor(PlayerEntity p){
		int armor = 0;
		for(EquipmentSlot a : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
			if(p.getEquippedStack(a).getItem() != Items.AIR) armor++;

		return armor < minArmor.get() ? true : false;
	}
	
	
	private boolean checkFriend(PlayerEntity p){
		return (ignoreFriends.get() && Friends.get().get(p.getName().asString()) != null);
	}

	
	private void add(int a){
		if(players.get(a) == null) players.put(a, 0);
		else players.put(a, players.get(a));
	}
	
	@EventHandler
	private void AttackEntity(AttackEntityEvent e){
		
		
		if(e.entity instanceof EndCrystalEntity){
			mc.world.getPlayers().forEach(p ->{
				if(checkTarget(p) && p.distanceTo(e.entity) < 8) add(p.getId());
			});
		} else if(e.entity instanceof PlayerEntity && checkTarget(e.entity)) add(e.entity.getId());
	}
	


	@EventHandler
	private void PacketEvent(PacketEvent.Receive e) {
		if(e.packet instanceof EntityStatusS2CPacket) {
			EntityStatusS2CPacket p = (EntityStatusS2CPacket) e.packet;
			
			if(p.getEntity(mc.world) instanceof PlayerEntity && checkTarget(p.getEntity(mc.world)) && players.containsKey(p.getEntity(mc.world).getId()) ) {
				if(p.getStatus() == 3) ezz(p.getEntity(mc.world));
				if(p.getStatus() == 35){
					int id = p.getEntity(mc.world).getId();
					if(players.get(id) == null) players.put(id, 1);
					else players.put(id, players.get(id) + 1);
				}
			}
		}
	}
	
	private boolean checkTarget(Entity a){
		PlayerEntity p = (PlayerEntity) a;
		return ( !p.isSpectator() && !p.isCreative() && !p.isInvulnerable() && !mc.player.equals(p) && !checkArmor(p) && !checkFriend(p) ) ? true : false;
	}
	
	
	private void ezz(Entity e){
		int id = e.getId();
		if(b.get() == Mode.Message) {
			if(players.get(id) == 0 && mc.player.distanceTo(e) < 8) mc.player.sendChatMessage(killMessages.get().get(Utils.random(0, killMessages.get().size())).replace("{player}", e.getName().getString()));
			else if(players.get(id) != 0 && mc.player.distanceTo(e) < 8)mc.player.sendChatMessage(killMessages.get().get(Utils.random(0, killMessages.get().size())).replace("{player}", e.getName().getString()));
		players.remove(id);
	}
	else if(b.get() == Mode.Client) {
		if(players.get(id) == 0 && mc.player.distanceTo(e) < 8) ChatUtils.info(killMessages.get().get(Utils.random(0, killMessages.get().size())).replace("{player}", e.getName().getString()));
		else if(players.get(id) != 0 && mc.player.distanceTo(e) < 8) ChatUtils.info(killMessages.get().get(Utils.random(0, killMessages.get().size())).replace("{player}", e.getName().getString()));
		players.remove(id);
	}
	}
		
}