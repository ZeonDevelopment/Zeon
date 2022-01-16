package dextro.zeon.addon.modules.Combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import dextro.zeon.addon.Zeon;
import dextro.zeon.addon.utils.BlockUtilsWorld;

public class SelfTrapPlus extends Module {
    public enum TopMode {
        Face,
        Top,
        Full
    }
    
    public enum Center {
    	OnActivate,
    	Always
    }
    
    public enum Mode {
    	MultiVersion,
    	MonoVersion
    }
    
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgOther = settings.createGroup("Other");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Mode> version = sgPlace.add(new EnumSetting.Builder<Mode>()
            .name("version-mode")
            .description("Version mode.")
            .defaultValue(Mode.MultiVersion)
            .build()
    );
    
    private final Setting<Integer> speed = sgPlace.add(new IntSetting.Builder()
            .name("delay")
            .description("How many ticks between block placements.")
            .defaultValue(1)
            .build()
    );
    
    private final Setting<TopMode> mode = sgPlace.add(new EnumSetting.Builder<TopMode>()
            .name("place-mode")
            .description("Place mode.")
            .defaultValue(TopMode.Top)
            .build()
    );
    
    private final Setting<Boolean> under = sgPlace.add(new BoolSetting.Builder()
            .name("under-place")
            .description("Place blocks under you.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> anticev = sgPlace.add(new BoolSetting.Builder()
            .name("anti-cev-breaker")
            .description("Place blocks that prevents playes uses cev-breaker.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> center = sgOther.add(new BoolSetting.Builder()
            .name("center")
            .description("Centers you on the block you are standing on before placing.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Center> centerTP = sgOther.add(new EnumSetting.Builder<Center>()
            .name("teleport-mode")
            .description("Teleport mode.")
            .defaultValue(Center.OnActivate)
            .visible(() -> center.get())
            .build()
    );
    
    private final Setting<Boolean> holeonly = sgOther.add(new BoolSetting.Builder()
            .name("only-in-hole")
            .description("Activates and place blcoks above u obly in hole.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> toggle = sgOther.add(new BoolSetting.Builder()
            .name("teleport-toggle")
            .description("Disables module when you teleported.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgOther.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Sends rotation packets to the server when placing.")
            .defaultValue(true)
            .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Renders a block overlay where the obsidian will be placed.")
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The color of the sides of the blocks being rendered.")
            .defaultValue(new SettingColor(204, 0, 0, 10))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The color of the lines of the blocks being rendered.")
            .defaultValue(new SettingColor(204, 0, 0, 255))
            .build()
    );

    private final List<BlockPos> placePositions = new ArrayList<>();
    private boolean placed;
    private int delay;
    private int speedBl;
    
    private final ArrayList<Vec3d> full = new ArrayList<Vec3d>() {{
        add(new Vec3d(0, 2, 0));
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};
    
    private final ArrayList<BlockPos> fullplace = new ArrayList<BlockPos>() {{
        add(new BlockPos(0, 2, 0));
        add(new BlockPos(1, 1, 0));
        add(new BlockPos(-1, 1, 0));
        add(new BlockPos(0, 1, 1));
        add(new BlockPos(0, 1, -1));
    }};

    private final ArrayList<Vec3d> face = new ArrayList<Vec3d>() {{
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};
    
    private final ArrayList<BlockPos> faceplace = new ArrayList<BlockPos>() {{
        add(new BlockPos(1, 1, 0));
        add(new BlockPos(-1, 1, 0));
        add(new BlockPos(0, 1, 1));
        add(new BlockPos(0, 1, -1));
    }};

    public SelfTrapPlus(){
        super(Zeon.Combat, "self-trap-plus", "Places obsidian above your head.");
    }

    @Override
    public void onActivate() {
        if (!placePositions.isEmpty()) placePositions.clear();
        speedBl = 0;
        placed = false;

        if (center.get() && centerTP.get() == Center.OnActivate) PlayerUtils.centerPlayer();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
    	speedBl = 0;
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

        if (!obsidian.found()) {
            placePositions.clear();
            return;
        }
        if (holeonly.get() && !BlockUtilsWorld.isSurrounded(mc.player)) { 
        	toggle();
        	return;
        	}
        if (center.get() && centerTP.get() == Center.OnActivate) PlayerUtils.centerPlayer();
     if(version.get() == Mode.MonoVersion)
     {
        findPlacePos();

        if (delay >= speed.get() && placePositions.size() > 0) {
            BlockPos blockPos = placePositions.get(placePositions.size() - 1);

            if (BlockUtils.place(blockPos, obsidian, rotate.get(), 50)) {
                placePositions.remove(blockPos);
                placed = true;
            }

            delay = 0;
        }
        else delay++;
    } else if (version.get() == Mode.MultiVersion) {
    	for (Vec3d b : getBlock()) {
            if (speedBl >= speed.get()) return;
            BlockPos ppos = mc.player.getBlockPos();
            BlockPos bb = ppos.add(b.x, b.y, b.z);
            if (BlockUtilsWorld.getBlock(bb) == Blocks.AIR) {
                BlockUtils.place(bb, obsidian, rotate.get(), 100, true);
                speedBl++;
            }
        }
    }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || placePositions.isEmpty()) return;
        for (BlockPos pos : placePositions) event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    private void findPlacePos() {
        placePositions.clear();
        BlockPos pos = mc.player.getBlockPos();

        switch (mode.get()) {
            case Full:
                add(pos.add(0, 2, 0));
                add(pos.add(1, 1, 0));
                add(pos.add(-1, 1, 0));
                add(pos.add(0, 1, 1));
                add(pos.add(0, 1, -1));
                break;
            case Top:
                add(pos.add(0, 2, 0));
                break;
            case Face:
                add(pos.add(1, 1, 0));
                add(pos.add(-1, 1, 0));
                add(pos.add(0, 1, 1));
                add(pos.add(0, 1, -1));

        }

        if (under.get()) add(pos.add(0, -1, 0));
        if (anticev.get()) add(pos.add(0, 3, 0));
    }
    

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof TeleportConfirmC2SPacket && toggle.get()) toggle();
    }
    
    private ArrayList<Vec3d> getBlock() {
        ArrayList<Vec3d> trapDesign = new ArrayList<Vec3d>();
        switch (mode.get()) {
            case Full: 
            	trapDesign.addAll(full);
            	
            case Top: 
            	trapDesign.add(new Vec3d(0, 2, 0)); 
            	
            case Face:
            	trapDesign.addAll(face); 
            	
        }
        if (under.get()) trapDesign.add(new Vec3d(0, -1, 0));
        if (anticev.get()) { trapDesign.add(new Vec3d(0, 3, 0));}
        return trapDesign;
    }


    private void add(BlockPos blockPos) {
        if (!placePositions.contains(blockPos) && mc.world.getBlockState(blockPos).getMaterial().isReplaceable() && mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), blockPos, ShapeContext.absent())) placePositions.add(blockPos);
    }
}