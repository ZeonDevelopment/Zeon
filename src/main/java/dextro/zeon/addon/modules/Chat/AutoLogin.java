package dextro.zeon.addon.modules.Chat;


import java.io.File;

import dextro.zeon.addon.Zeon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class AutoLogin extends Module {

    public AutoLogin() {
        super(Zeon.Misc, "auto-login", "Auto /login on join.");
    }

	@Override
	public void onActivate() {
		
	}
	
    @EventHandler
    private void Send(PacketEvent.Send e) {
    	if(e.packet instanceof ChatMessageC2SPacket){
    		String s = ((ChatMessageC2SPacket)e.packet).getChatMessage();

    		if(s.startsWith("/login ") || s.startsWith("/l ") || s.startsWith("/reg ") || s.startsWith("/register ")){
    			String[] text = s.split(" ");
    			if(text.length > 1) save(mc.getCurrentServerEntry().address.toLowerCase(), mc.player.getName().asString().toLowerCase(), text[1]);
    		}

    	}
    }


    File file = new File(MeteorClient.FOLDER, "Login.nbt");
    String prefix = String.format("§8§l［§b%s§8§l］ ",name);

	public NbtCompound getTag(){
		NbtCompound tag = new NbtCompound();
		try {
			if(file.exists()){
				tag = NbtIo.read(file);
			} else {
				file.createNewFile();
			}
		} catch (Exception ex) {}
		return tag;
	}

	public void login(){
		NbtCompound tag = getTag();
		String name = mc.player.getName().asString().toLowerCase();
		String server = mc.getCurrentServerEntry().address.toLowerCase();
		if(tag.contains(server) && tag.getCompound(server).contains(name)) {
			mc.player.sendChatMessage("/login " + tag.getCompound(server).getString(name));
		} else {
			info(String.format(prefix+"§cPassword for player §f%s§c didnt found!",name));
		}
	}

	public void save(String server, String name, String pass){

		NbtCompound tag = getTag();

		if(tag.contains(server) && tag.getCompound(server).contains(name) && tag.getCompound(server).getString(name).equals(pass)) return;


		if(!tag.contains(server)) tag.put(server, new NbtCompound());
		tag.getCompound(server).putString(name, pass);

		try {
			NbtIo.write(tag, file);
			info(String.format(prefix+"§aPassword for player §f%s§a on server §f%s§a just save!",name,server));
		} catch (Exception e) {}
	}




}