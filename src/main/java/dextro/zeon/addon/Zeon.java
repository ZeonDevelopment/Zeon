package dextro.zeon.addon;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dextro.zeon.addon.commands.Armor;
import dextro.zeon.addon.commands.Count;
import dextro.zeon.addon.hud.client.IconHud;
import dextro.zeon.addon.hud.client.WatermarkHud;
import dextro.zeon.addon.hud.client.WelcomeHud;
import dextro.zeon.addon.hud.items.CrystalHud;
import dextro.zeon.addon.hud.items.EXPHud;
import dextro.zeon.addon.hud.items.ObsidianHud;
import dextro.zeon.addon.modules.Chat.ActionLogger;
import dextro.zeon.addon.modules.Chat.ArmorNotifer;
import dextro.zeon.addon.modules.Chat.AutoLogin;
import dextro.zeon.addon.modules.Chat.BurrowAlert;
import dextro.zeon.addon.modules.Chat.NewAutoEz;
import dextro.zeon.addon.modules.Chat.SurroundAlert;
import dextro.zeon.addon.modules.Combat.AntiBedPlus;
import dextro.zeon.addon.modules.Combat.AutoCityPlus;
import dextro.zeon.addon.modules.Combat.BedAuraPlus;
import dextro.zeon.addon.modules.Combat.BurrowBreaker;
import dextro.zeon.addon.modules.Combat.CevBreakerTest;
import dextro.zeon.addon.modules.Combat.CrystalAuraRewrite;
import dextro.zeon.addon.modules.Combat.CustomAutoTotem;
import dextro.zeon.addon.modules.Combat.FastBreak;
import dextro.zeon.addon.modules.Combat.SelfTrapPlus;
import dextro.zeon.addon.modules.Combat.SurroundPlus;
import dextro.zeon.addon.modules.Misc.DiscordPrecencePlus;
import dextro.zeon.addon.modules.Misc.ExtraNuker;
import dextro.zeon.addon.modules.Misc.ItemShrader;
import meteordevelopment.meteorclient.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class Zeon extends MeteorAddon {
	public static final Category Combat = new Category("Zeon", Items.END_CRYSTAL.getDefaultStack());
	public static final Category Chat = new Category("Zeon Chat", Items.SPECTRAL_ARROW.getDefaultStack());
	public static final Category Misc = new Category("Zeon Misc+", Items.GRASS_BLOCK.getDefaultStack());
	public static final String VERSION = "v0.2";

	boolean NEED_LOGIN = false;

	@EventHandler(priority = 500)
	private void AutoLogin(PacketEvent.Receive e) {
		if(e.packet instanceof GameMessageS2CPacket && Modules.get().get(AutoLogin.class).isActive()){
			String s = ((GameMessageS2CPacket)e.packet).getMessage().getString();

			if(s.contains("/captcha ")) {
				Pattern p = Pattern.compile("/captcha \\w+");
				Matcher m = p.matcher(s);
				m.find();
				MinecraftClient.getInstance().player.sendChatMessage(m.group());
			}

			if(s.toLowerCase().contains("/login ")) NEED_LOGIN = true;

		}
	}



	@EventHandler
	private void onTick1(TickEvent.Post e) {
		if(NEED_LOGIN && MinecraftClient.getInstance().currentScreen == null && ((MinecraftClientAccessor) MinecraftClient.getInstance()).getFps() >=59 ) {
			NEED_LOGIN = false;
			Modules.get().get(AutoLogin.class).login();
		}
	}


	@EventHandler
	private void AutoLoginCOMMANDS(PacketEvent.Send e) {
		if(e.packet instanceof ChatMessageC2SPacket){
			String s = ((ChatMessageC2SPacket)e.packet).getChatMessage();

			if(s.equalsIgnoreCase("/LOGIN") || s.equalsIgnoreCase("/L")){
				e.cancel();
				Modules.get().get(AutoLogin.class).login();
			}
		}
	}

	@Override
	public void onInitialize() {

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("dextro.zeon.addon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
		Modules.get().add(new ActionLogger());
		Modules.get().add(new AntiBedPlus());
		Modules.get().add(new SurroundAlert());
		Modules.get().add(new BurrowAlert());
		Modules.get().add(new ArmorNotifer());
		Modules.get().add(new BurrowBreaker());
		Modules.get().add(new AutoLogin());
		Modules.get().add(new BedAuraPlus());
		Modules.get().add(new CevBreakerTest());
		Modules.get().add(new CustomAutoTotem());
		Modules.get().add(new CrystalAuraRewrite());
		Modules.get().add(new DiscordPrecencePlus());
		Modules.get().add(new ExtraNuker());
		Modules.get().add(new SelfTrapPlus());
		Modules.get().add(new SurroundPlus());
		Modules.get().add(new FastBreak());
		Modules.get().add(new AutoCityPlus());
		Modules.get().add(new ItemShrader());
		Modules.get().add(new NewAutoEz());

		// Commands
		Commands.get().add(new Armor());
		Commands.get().add(new Count());

		// HUD
		HUD hud = Modules.get().get(HUD.class);
		hud.elements.add(new IconHud(hud));
		hud.elements.add(new CrystalHud(hud));
		hud.elements.add(new EXPHud(hud));
		hud.elements.add(new ObsidianHud(hud));
		hud.elements.add(new WatermarkHud(hud));
		hud.elements.add(new WelcomeHud(hud));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(Combat);
		Modules.registerCategory(Misc);
		Modules.registerCategory(Chat);
	}
}
