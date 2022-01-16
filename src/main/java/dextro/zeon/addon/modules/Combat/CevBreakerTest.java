package dextro.zeon.addon.modules.Combat;

import dextro.zeon.addon.Zeon;
import dextro.zeon.addon.utils.BlockUtilsWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CevBreakerTest extends Module {

    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgRender = settings.createGroup("Render");
    
    private final Setting<Double> distance = sgPlace.add(new DoubleSetting.Builder()
            .name("range")
            .description("Range where player will targeting.")
            .defaultValue(7)
            .min(0)
            .sliderMax(6)
            .max(6)
            .build()
    );
    
    private final Setting<Integer> delay = sgPlace.add(new IntSetting.Builder()
            .name("delay")
            .description("The amount of delay in ticks before placing.")
            .defaultValue(4)
            .min(0)
            .sliderMax(20)
    		.build()
    		);
    
	private final Setting<Boolean> instant = sgBreak.add(new BoolSetting.Builder()
			.name("instant")
			.description("Allow insta break for Cev.")
			.defaultValue(false)
			.build()
		);
	
	   private final Setting<Integer> instantDelay = sgBreak.add(new IntSetting.Builder()
	            .name("delay")
	            .description("The amount of delay in ticks before mining with insta break.")
	            .defaultValue(4)
	            .min(0)
	            .sliderMax(20)
	            .visible(() -> instant.get())
	    		.build()
	    		);
	
	    
	private final Setting<Boolean> rotate = sgMisc.add(new BoolSetting.Builder()
			.name("rotate")
			.description("Automatically rotate to block that placing.")
			.defaultValue(true)
			.build()
		);
	
	private final Setting<Boolean> ak47 = sgMisc.add(new BoolSetting.Builder()
			.name("remove-crystals")
			.description("...")
			.defaultValue(true)
			.build()
		);
	
	private final Setting<Boolean> chat = sgMisc.add(new BoolSetting.Builder()
			.name("chat-info")
			.description("...")
			.defaultValue(false)
			.build()
		);
	
	private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
			.name("render")
			.description("...")
			.defaultValue(true)
			.build()
		);
    
	   private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
	            .name("shape-mode")
	            .description("How the shapes are rendered.")
	            .defaultValue(ShapeMode.Lines)
	            .visible(()->render.get())
	            .build()
	    );

	    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
	            .name("side-color")
	            .description("The side color.")
	            .defaultValue(new SettingColor(255, 255, 255, 75))
	            .visible(()->render.get())
	            .build()
	    );

	    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
	            .name("line-color")
	            .description("The line color.")
	            .defaultValue(new SettingColor(255, 255, 255, 255))
	            .visible(()->render.get())
	            .build()
	    );
   
    public CevBreakerTest() 
    {
    	super(Zeon.Combat, "cev-breaker", "Place crystals on obsidian above target and break/attack obsidian/crystals.");
    }
    
    public PlayerEntity target;
    boolean offhand = false;
    boolean started = false;
    boolean placed = false;
    BlockPos targetPos;
    int ticks = 0;
    int instaTicks = 0;

    @Override
    public void onActivate() {
    	
        started = false;
        target = null;
        ticks = 0;
    }

    @Override
    public void onDeactivate() {
        started = false;
        target = null;
        ticks = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre e) {
        target = getTarget();
        if (target != null) {
            targetPos = target.getBlockPos().add(0, 2, 0);
            Vec3d vec = new Vec3d(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if (!(mc.world.getBlockState(targetPos.up()).getBlock() instanceof AirBlock)) return;
            if (ticks == delay.get()) {
                if (InvUtils.findInHotbar(Items.OBSIDIAN).getSlot() == -1) return;
                InvUtils.swap(InvUtils.findInHotbar(Items.OBSIDIAN).getSlot(), false);
                if (mc.world.getBlockState(targetPos).getBlock() instanceof AirBlock) {
                	if(chat.get())
                		{
                		info("Placing");
                		}
                    if (rotate.get()) {
                    	BlockUtilsWorld.rotateBl(targetPos);
                    }
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(vec, Direction.UP, targetPos, true));
                }
            }
            if (ticks == delay.get()) {
                if (InvUtils.findInHotbar(Items.END_CRYSTAL).getSlot() == -1 && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
                    return;
                if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
                     if(InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() != -1) InvUtils.swap(InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot(), false);
                     else if((InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() != -1))InvUtils.swap(InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot(), false);
                     else if((InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot() != -1))InvUtils.swap(InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot(), false);
                     else return;
                    offhand = true;
                } else {
                    InvUtils.swap(InvUtils.findInHotbar(Items.END_CRYSTAL).getSlot(), false);
                    offhand = false;
                }
                if (rotate.get()) {
                	BlockUtilsWorld.rotateBl(targetPos);
                }
                mc.interactionManager.interactBlock(mc.player, mc.world, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(vec, Direction.DOWN, targetPos, true));
                placed = true;
            }
            if (ticks == delay.get()) {
                if ((InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot() == -1)) return;
               if(InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() != -1) InvUtils.swap(InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot(), false);
               else if((InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() != -1))InvUtils.swap(InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot(), false);
               else if((InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() == -1) && (InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot() != -1))InvUtils.swap(InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot(), false);
               else return;
               if (!instant.get()) {
            	   if(chat.get())
           		{
                    info("Breaking");
           		}
                    if (rotate.get()) BlockUtilsWorld.rotateBl(targetPos);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
                }
            }
            if (ticks >= delay.get()) {
                if (mc.world.getBlockState(targetPos).getBlock() instanceof AirBlock) {
                    if (getCrystal(target) != null) {
                        if (rotate.get()) BlockUtilsWorld.rotateBl(getCrystal(target).getBlockPos());
                        mc.interactionManager.attackEntity(mc.player, getCrystal(target));
                        if (ak47.get()) getCrystal(target).remove(RemovalReason.KILLED);
                    }
                    placed = false;
                    ticks = -1;
                } else if (instant.get()) {
                	if(chat.get())
            		{
                   info("Breaking");
            		}
                    if (started == false) {
                        if (rotate.get()) BlockUtilsWorld.rotateBl(targetPos);
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
                        started = true;
                    }
                    if (instaTicks >= instantDelay.get()) {
                        instaTicks = 0;
                        if (shouldMine(targetPos)) {
                            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
                            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }
                    } else instaTicks++;
                }
            }
            if (placed && getCrystal(target) == null) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                if (InvUtils.findInHotbar(Items.END_CRYSTAL).getSlot() == -1) return;
                InvUtils.swap(InvUtils.findInHotbar(Items.END_CRYSTAL).getSlot(), false);
                if (rotate.get()) BlockUtilsWorld.rotateBl(targetPos);
                mc.interactionManager.interactBlock(mc.player, mc.world, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(vec, Direction.DOWN, targetPos, true));
                InvUtils.swap(prevSlot, false);
            }
            ticks++;
        } else {
            targetPos = null;
            if(chat.get())
    		{
            info("Nothing");
    		}
        }
    }

    @EventHandler
    private void onRender(Render3DEvent e) {
        if (render.get() && targetPos != null) {
        	e.renderer.box( targetPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private EndCrystalEntity getCrystal(PlayerEntity target) {
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EndCrystalEntity) {
                if (Math.ceil(e.getX()) == Math.ceil(target.getX()) && Math.ceil(e.getZ()) == Math.ceil(target.getZ()))
                    return (EndCrystalEntity) e;
            }
        }
        return null;
    }

    private boolean shouldMine(BlockPos bp) {
        if (bp.getY() == -1) return false;
        if (mc.world.getBlockState(bp).getHardness(mc.world, bp) < 0) return false;
        if (mc.world.getBlockState(bp).isAir()) return false;
        return mc.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE || mc.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE;
    }

    private PlayerEntity getTarget() {
        for (Entity e : mc.world.getEntities()) {
            if (mc.player.distanceTo(e) <= distance.get() && e instanceof PlayerEntity && e != mc.player) {
                return (PlayerEntity) e;
            }
        }
        return null;
    }

}