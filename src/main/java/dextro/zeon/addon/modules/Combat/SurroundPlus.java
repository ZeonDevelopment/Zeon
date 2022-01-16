package dextro.zeon.addon.modules.Combat;

import java.math.BigDecimal;
import java.util.ArrayList;

import dextro.zeon.addon.Zeon;
import dextro.zeon.addon.utils.BlockUtilsWorld;
import dextro.zeon.addon.utils.Ezz;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SurroundPlus extends Module {
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgProtector = settings.createGroup("Protector");
    private final SettingGroup sgRender = settings.createGroup("Render");
    
    public enum SurrMode{
    	Normal,
    	Double,
    	Full
    }
    
    public enum ecenter {
        fast,
        legit,
        NoTP
    }
    
    public enum Version {
    	Multi_Version("Multi Version"),
    	Mono_Version("Mono Version");
    	private final String title;
    	Version(String title) {
            this.title = title;
        }
        @Override
        public String toString() {
            return title;
        }
    }
    
	private final Setting<Version> version = sgPlace.add(new EnumSetting.Builder<Version>()
			.name("version")
			.description("Version of server where u will be pvp.")
			.defaultValue(Version.Multi_Version)
			.build()
		);
  
    private final Setting<Integer> tickDelay = sgPlace.add(new IntSetting.Builder()
    		.name("Delay")
    		.description("Delay per ticks.")
    		.defaultValue(1)
    		.min(0)
    		.max(20)
    		.sliderMin(0)
    		.sliderMax(20)
    		.build()
    		);
    
	private final Setting<ecenter> center = sgPlace.add(new EnumSetting.Builder<ecenter>()
		.name("centerTP")
		.description("Teleport to center block.")
		.defaultValue(ecenter.legit)
		.build()
	);
	
    
	private final Setting<SurrMode> mode = sgPlace.add(new EnumSetting.Builder<SurrMode>()
			.name("structure-mode")
			.description("Mode of the surround.")
			.defaultValue(SurrMode.Normal)
			.build()
	);
	
	private final Setting<Boolean> selfProtector = sgProtector.add(new BoolSetting.Builder()
			.name("self-protector")
			.description("Automatically breaks crystal near ur surround.")
			.defaultValue(true)
			.build()
		);
	
    private final Setting<Integer> range = sgProtector.add(new IntSetting.Builder()
            .name("distance")
            .description("range.")
            .defaultValue(0)
            .min(0)
            .sliderMax(7)
            .visible(selfProtector::get)
            .build()
            );
	
	private final Setting<Boolean> anti = sgMisc.add(new BoolSetting.Builder()
			.name("anti-crystal-aura")
			.description("Anti Break your surround(place ender-chests).")
			.defaultValue(true)
			.build()
		);
	
	private final Setting<Boolean> anticev = sgMisc.add(new BoolSetting.Builder()
			.name("anti-cev-breaker")
			.description("Placing block 2 blocks up from your head.")
			.defaultValue(false)
			.build()
		);

	private final Setting<Boolean> onlyOnGround = sgMisc.add(new BoolSetting.Builder()
		.name("only-on-ground")
		.description("Works only when you standing on blocks.")
		.defaultValue(false)
		.build()
	);
	
	private final Setting<Boolean> disableOnJump = sgMisc.add(new BoolSetting.Builder()
		.name("disable-on-jump")
		.description("Automatically disables when you jump.")
		.defaultValue(true)
		.build()
	);
	
	private final Setting<Boolean> rotate = sgMisc.add(new BoolSetting.Builder()
		.name("rotate")
		.description("Automatically faces towards the obsidian being placed.")
		.defaultValue(false)
		.build());

	private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
			.name("render")
			.description("Render surround blocks.")
			.defaultValue(true)
			.build());

	   private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
	            .name("shape-mode")
	            .description("How the shapes are rendered.")
	            .defaultValue(ShapeMode.Lines)
	            .visible(render::get)
	            .build()
	    );

	    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
	            .name("side-color")
	            .description("The side color.")
	            .defaultValue(new SettingColor(255, 255, 255, 75))
	            .visible(render::get)
	            .build()
	    );

	    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
	            .name("line-color")
	            .description("The line color.")
	            .defaultValue(new SettingColor(255, 255, 255, 255))
	            .visible(render::get)
	            .build()
	    );
	
	public SurroundPlus() {
		super(Zeon.Combat, "surround-plus", "Surround+");
	}
    private static final MinecraftClient mc = MinecraftClient.getInstance();

	
    private int ticks;
	BlockPos pos = null;
    
    private final ArrayList<Vec3d> norm = new ArrayList<Vec3d>() {{
    	add(new Vec3d(0, -1, 0));
    	add(new Vec3d(1, 0, 0));
        add(new Vec3d(-1, 0, 0));
        add(new Vec3d(0, 0, 1));
        add(new Vec3d(0, 0, -1));
    }};
    
    private final ArrayList<Vec3d> doub = new ArrayList<Vec3d>() {{
    	add(new Vec3d(0, -1, 0));
    	add(new Vec3d(1, 0, 0));
        add(new Vec3d(-1, 0, 0));
        add(new Vec3d(0, 0, 1));
        add(new Vec3d(0, 0, -1));
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};
    
    
    private final ArrayList<Vec3d> full = new ArrayList<Vec3d>() {{
    	add(new Vec3d(0, -1, 0));
    	add(new Vec3d(1, 0, 0));
        add(new Vec3d(-1, 0, 0));
        add(new Vec3d(0, 0, 1));
        add(new Vec3d(0, 0, -1));
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
        add(new Vec3d(1, 0, 1));
    	add(new Vec3d(-1, 0, 1));
        add(new Vec3d(-1, 0, -1));
        add(new Vec3d(1, 0, -1));
        add(new Vec3d(2, 0, 0));
        add(new Vec3d(0, 0, 2));
        add(new Vec3d(-2, 0, 0));
        add(new Vec3d(0, 0, -2));
        add(new Vec3d(0, 2, 0));
        add(new Vec3d(1, 2, 0));
        add(new Vec3d(0, 2, 1));
        add(new Vec3d(-1, 2, 0));
        add(new Vec3d(0, 2, -1));
        add(new Vec3d(0, 3, 0));
    }};
    
    @Override
    public void onActivate() {

        ticks = 0;
        if(center.get() == ecenter.fast){
	    	double tx=0,tz=0;
	
	    	Vec3d p = mc.player.getPos(); 
	    	
		   	 if (p.x>0 && gp(p.x)<3) tx=0.3;
			 if (p.x>0 && gp(p.x)>6) tx=-0.3;
			 if (p.x<0 && gp(p.x)<3) tx=-0.3;
			 if (p.x<0 && gp(p.x)>6) tx=0.3;
		
			 if (p.z>0 && gp(p.z)<3) tz=0.3;
			 if (p.z>0 && gp(p.z)>6) tz=-0.3;
			 if (p.z<0 && gp(p.z)<3) tz=-0.3;
			 if (p.z<0 && gp(p.z)>6) tz=0.3;
		
			 if(tx!=0 || tz!=0){
		    	 double posx = mc.player.getX() + tx;
		         double posz = mc.player.getZ() + tz;
		         mc.player.updatePosition(posx, mc.player.getY(), posz);
		         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
		    }
        }
    	}
    


    private long gp(double v) {
    	   BigDecimal v1 = BigDecimal.valueOf(v);
	       BigDecimal v2 = v1.remainder(BigDecimal.ONE);
	       return Byte.valueOf(String.valueOf(String.valueOf(v2).replace("0.", "").replace("-", "").charAt(0)));
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mode.get() == SurrMode.Full) { error("You cant use this mode in free version! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
        toggle();
     	 return;}
        if(selfProtector.get() == true) {
       error("You cant use self protector in free version! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
       toggle();
  	 return;
    }
        if (ticks >= tickDelay.get()) {
            ticks = 0;
            
            if(center.get() == ecenter.legit){
        	
	    	double tx=0,tz=0;
	    	Vec3d p = mc.player.getPos(); 
		   	 if (p.x>0 && gp(p.x)<3) tx=0.185;
			 if (p.x>0 && gp(p.x)>6) tx=-0.185;
			 if (p.x<0 && gp(p.x)<3) tx=-0.185;
			 if (p.x<0 && gp(p.x)>6) tx=0.185;
		
			 if (p.z>0 && gp(p.z)<3) tz=0.185;
			 if (p.z>0 && gp(p.z)>6) tz=-0.185;
			 if (p.z<0 && gp(p.z)<3) tz=-0.185;
			 if (p.z<0 && gp(p.z)>6) tz=0.185;	

		
			 if(tx!=0 || tz!=0){
		    	 double posx = mc.player.getX() + tx;
		         double posz = mc.player.getZ() + tz;
		         mc.player.updatePosition(posx, mc.player.getY(), posz);
		         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
		         return;
		    }
        }
    	
    	
        if (disableOnJump.get() && mc.options.keyJump.isPressed()) {
            toggle();
            return;
        }

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;


  if(version.get() == Version.Mono_Version) {
    if(mode.get() == SurrMode.Normal) {
	 if(p(0, -1, 0)) return;
	if(p(1, 0, 0)) return;
        if(p(-1, 0, 0)) return;
        if(p(0, 0, 1)) return;
        if(p(0, 0, -1)) return;
        if(anticev.get() == true) {
       	 if(p(1, 1, 0)) return;
            if(p(0, 1, 1)) return;
            if(p(-1, 1, 0)) return;
            if(p(0, 1, -1)) return;
            if(p(0, 2, 0)) return;
            if(p(0, 3, 0)) return;
            
       }
        if(anti.get()) {
            if(e(1, -1, 0)) return;
            if(e(-1, -1, 0)) return;
            if(e(0, -1, 1)) return;
            if(e(0, -1, -1)) return;
    }
       else {
    	   if(p(1, -1, 0)) return;
           if(p(-1, -1, 0)) return;
           if(p(0, -1, 1)) return;
           if(p(0, -1, -1)) return;
       }
}

        if (mode.get() == SurrMode.Double) {
        	if(p(0, -1, 0)) return;
        	if(p(1, 0, 0)) return;
            if(p(-1, 0, 0)) return;
            if(p(0, 0, 1)) return;
            if(p(0, 0, -1)) return;
            if(p(1, 1, 0)) return;
            if(p(-1, 1, 0)) return;
            if(p(0, 1, 1)) return;
            if(p(0, 1, -1)) return;
            
            if(anticev.get() == true) {
           	 if(p(1, 1, 0)) return;
                if(p(0, 1, 1)) return;
                if(p(-1, 1, 0)) return;
                if(p(0, 1, -1)) return;
                if(p(0, 2, 0)) return;
                if(p(0, 3, 0)) return;
                
           }
            if(anti.get()) {
                if(e(1, -1, 0)) return;
                if(e(-1, -1, 0)) return;
                if(e(0, -1, 1)) return;
                if(e(0, -1, -1)) return;
        }
           else {
        	   if(p(1, -1, 0)) return;
               if(p(-1, -1, 0)) return;
               if(p(0, -1, 1)) return;
               if(p(0, -1, -1)) return;
           }
        }
        
      
           

  }else if(version.get() == Version.Multi_Version) {
	  if (disableOnJump.get() && mc.options.keyJump.isPressed()) {
          toggle();
          return;
      }
      if (onlyOnGround.get() && !mc.player.isOnGround()) return;
      if (isVecComplete(getSurrDesign())) {
      } else {
          BlockPos ppos = mc.player.getBlockPos();
          for (Vec3d b : getSurrDesign()) {
              BlockPos bb = ppos.add(b.x, b.y, b.z);
              if (getBlock(bb) == Blocks.AIR) {
                      BlockUtils.place(bb, InvUtils.findInHotbar(Items.OBSIDIAN), rotate.get(), 100, false);
                  
                 
              }
          }
      }
  }
        }else ticks++;

    }
    
    private ArrayList<Vec3d> getSurrDesign() {
        ArrayList<Vec3d> surrDesign = new ArrayList<Vec3d>(norm);
        if (mode.get() == SurrMode.Double) surrDesign.addAll(doub);
        return surrDesign;
    }

    @EventHandler
    private void onRender(Render3DEvent e) {
        if (!render.get()) return;
        if(mc.player.getBlockPos().south() != null) e.renderer.box( mc.player.getBlockPos().south(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        if(mc.player.getBlockPos().west() != null) e.renderer.box( mc.player.getBlockPos().west(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        if(mc.player.getBlockPos().north() != null) e.renderer.box( mc.player.getBlockPos().north(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        if(mc.player.getBlockPos().east() != null) e.renderer.box( mc.player.getBlockPos().east(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
    
    
    private boolean p(int x, int y, int z) {
    	return Ezz.BlockPlace(Ezz.SetRelative(x, y, z), InvUtils.findInHotbar(Items.OBSIDIAN).getSlot(), rotate.get());
    
    }
    private boolean e(int x, int y, int z) {
    	return Ezz.BlockPlace(Ezz.SetRelative(x, y, z), InvUtils.findInHotbar(Items.ENDER_CHEST).getSlot(), rotate.get());
    }
    
    public static boolean isVecComplete(ArrayList<Vec3d> vlist) {
        BlockPos ppos = mc.player.getBlockPos();
        for (Vec3d b: vlist) {
            BlockPos bb = ppos.add(b.x, b.y, b.z);
            if (getBlock(bb) == Blocks.AIR) return false;
        }
        return true;
    }
    
    public static Block getBlock(BlockPos p) {
        if (p == null) return null;
        return mc.world.getBlockState(p).getBlock();
    }

}
