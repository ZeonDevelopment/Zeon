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
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class BedAuraPlus extends Module {
    public enum SuperMode {
    	immunity,
    	ignor_immunity,
    	MomentBreak,
    	TestMoment,
    	Test,
    	None
    	}
    public enum Prioritet {
    	Head,
    	Face,
    	Legs
    }
    
    public enum Direct {
    	EAST,
    	WEST,
    	SOUTH,
    	NORTH
    }

    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgTarget = settings.createGroup("Target");
    private final SettingGroup sgExtra = settings.createGroup("Extra");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Place

    private final Setting<SuperMode> mode = sgBreak.add(new EnumSetting.Builder<SuperMode>()
            .name("improved-place-mode")
            .description("How to select the position to place.")
            .defaultValue(SuperMode.None)
            .build()
    );
  
    private final Setting<Boolean> suicide = sgPlace.add(new BoolSetting.Builder()
            .name("allow-suicide")
            .description("Allows suicide mode for BedAura+. Suicide for friends and you.")
            .defaultValue(true)
            .build()
    );
    
    
    private final Setting<Boolean> autoBr = sgBreak.add(new BoolSetting.Builder()
            .name("auto-break")
            .description("Automaticly break.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> autoB = sgBreak.add(new BoolSetting.Builder()
            .name("self-trap-break")
            .description("Automaticly break target's self-trap/surround/head.")
            .defaultValue(true)
            .visible(() -> autoBr.get() == true)
            .build()
    );
    
    private final Setting<Prioritet> autoBreak = sgBreak.add(new EnumSetting.Builder<Prioritet>()
            .name("break-priority")
            .description("How to select the position to break.")
            .defaultValue(Prioritet.Face)
            .visible(() -> autoB.get() == true)
            .build()
    );
    
    private final Setting<Boolean> webBreak = sgBreak.add(new BoolSetting.Builder()
            .name("web-break")
            .description("Automaticly break target's webs.")
            .defaultValue(true)
            .visible(() -> autoBr.get() == true)
            .build()
    );
    
    private final Setting<Boolean> burrowBreak = sgBreak.add(new BoolSetting.Builder()
            .name("burrow-break")
            .description("Automaticly break target's burrow.")
            .defaultValue(true)
            .visible(() -> autoBr.get() == true)
            .build()
    );

    // Pause

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-eat")
            .description("Pauses while eating.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-drink")
            .description("Pauses while drinking potions.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-mine")
            .description("Pauses while mining blocks.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> pauseOnBurrow = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-burrow")
            .description("Pauses while target in burrow.")
            .defaultValue(true)
            .build()
    );
    
    // Misc

    private final Setting<Boolean> ai = sgMisc.add(new BoolSetting.Builder()
            .name("enable-smart-mode")
            .description("Enable smart mode for ba.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> autoSwitch = sgMisc.add(new BoolSetting.Builder()
            .name("switch")
            .description("Switches to a bed automatically.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> swing = sgMisc.add(new BoolSetting.Builder()
            .name("swing")
            .description("Swinges a hand automaticly.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> autoMove = sgMisc.add(new BoolSetting.Builder()
            .name("auto-move")
            .description("Moves beds into a selected slot.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> autoMoveSlot = sgMisc.add(new IntSetting.Builder()
            .name("move-slot")
            .description("The slot Auto Move.")
            .defaultValue(9)
            .min(1)
            .sliderMin(1)
            .max(9)
            .sliderMax(9)
            .visible(() -> autoMove.get() == true)
            .build()
    );

    private final Setting<Boolean> antifriendpop = sgExtra.add(new BoolSetting.Builder()
            .name("anti-friend-pop")
            .description("Anti friend pop.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Double> maxFriendDamage = sgExtra.add(new DoubleSetting.Builder()
            .name("max-friend-dmg")
            .description(".")
            .defaultValue(7)
            .min(0)
            .sliderMax(20)
            .max(20)
            .visible(() -> antifriendpop.get())         
            .build()
    );

    private final Setting<Double> minDamage = sgMisc.add(new DoubleSetting.Builder()
            .name("min-damage")
            .description("The minimum damage to inflict on your target.")
            .defaultValue(7)
            .min(0)
            .sliderMax(20)
            .max(20)
            .build()
    );

    private final Setting<Double> maxSelfDamage = sgMisc.add(new DoubleSetting.Builder()
            .name("max-self-dmg")
            .description(".")
            .defaultValue(7)
            .min(0)
            .sliderMax(20)
            .max(20)
            .build()
    );

    private final Setting<Double> minHealth = sgMisc.add(new DoubleSetting.Builder()
            .name("min-health")
            .description("The minimum health required for Bed Aura to work.")
            .defaultValue(4)
            .min(0)
            .sliderMax(36)
            .max(36)
            .build()
    );
    
    private final Setting<Double> targetRange = sgTarget.add(new DoubleSetting.Builder()
            .name("range")
            .description("The maximum range for players to be targeted.")
            .defaultValue(4)
            .min(0)
            .sliderMax(5)
            .build()
    );

    private final Setting<SortPriority> priority = sgTarget.add(new EnumSetting.Builder<SortPriority>()
            .name("priority")
            .description("How to select the player to target.")
            .defaultValue(SortPriority.LowestHealth)
            .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Renders the block where it is placing a bed.")
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("place-side-color")
            .description("The side color for positions to be placed.")
            .defaultValue(new SettingColor(0, 0, 0, 75))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("place-line-color")
            .description("The line color for positions to be placed.")
            .defaultValue(new SettingColor(15, 255, 211, 255))
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );
    

    private Direction direction;
    private PlayerEntity target;
    private int delay;
    private BlockPos placePos, placePos1;
    private BlockPos breakPos;
    private BlockPos stb;
    private CardinalDirection d;
    
    public BedAuraPlus(){
        super(Zeon.Combat, "bed-aura-plus", "Bed Aura+");
    }

    @Override
    public void onActivate() {   
        delay = 10;
        direction = Direction.EAST;
	    d = CardinalDirection.East;
    }
    @EventHandler
    private void Pre(TickEvent.Pre event) {
        if(autoBr.get()) {
       	 error("You cant use auto break in free version! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
       	 toggle();
       	 return;
           }
        
       if(mode.get() != SuperMode.None)
       {
       	 error("You cant use this mode in free version! Using None mode! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
       	 toggle();
       	 return;
       }
       
       if(ai.get()) {
         	 error("You cant use smart mode for bedaura+ in free version! Buy Zeon here -> https://ko-fi.com/s/8770cf080d");
         	 toggle();
         	 return;
             }
       
        if (mc.world.getDimension().isBedWorking()) {
            error("You are in the Overworld... disabling!");
            this.toggle();
            return;
        }
        target = TargetUtils.getPlayerTarget(targetRange.get(), priority.get());
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if(pauseOnBurrow.get() && BlockUtilsWorld.isBurrowed(target)) return;
       
        	
        
        if (EntityUtils.getTotalHealth(mc.player) <= minHealth.get()) return;

        target = TargetUtils.getPlayerTarget(targetRange.get(), priority.get());

        if (target == null) {
            placePos = null;
            breakPos = null;
            return;
        }
        
    

    	
    	if (breakPos == null) {
            placePos = getPlacePos(target);
    	}
        if ( InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).getCount() != -1) {
        	if (autoMove.get()) doAutoMove();
   			placePos1 = getPlacePos(target);
   			if (delay <= 0 && doPlace(placePos1)) {
                delay = 10;
            }
            else {
                delay--;
            }

            if (breakPos == null) breakPos = doBreakPos();
            doBreak(breakPos);
               		}
    }


    private BlockPos getPlacePos(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        //head
        if (checkPlace(Direction.NORTH, target, true, false, false)) return targetPos.up().north();
        if (checkPlace(Direction.SOUTH, target, true, false, false)) return targetPos.up().south();
        if (checkPlace(Direction.EAST, target, true, false, false)) return targetPos.up().east();
        if (checkPlace(Direction.WEST, target, true, false, false)) return targetPos.up().west();
        //down
        if (checkPlace(Direction.NORTH, target, false, false, true)) return targetPos.down().north();
        if (checkPlace(Direction.SOUTH, target, false, false, true)) return targetPos.down().south();
        if (checkPlace(Direction.EAST, target, false, false, true)) return targetPos.down().east();
        if (checkPlace(Direction.WEST, target, false, false, true)) return targetPos.down().west();
        //legs
        if (checkPlace(Direction.NORTH, target, false, false, false)) return targetPos.north();
        if (checkPlace(Direction.SOUTH, target, false, false, false)) return targetPos.south();
        if (checkPlace(Direction.EAST, target, false, false, false)) return targetPos.east();
        if (checkPlace(Direction.WEST, target, false, false, false)) return targetPos.west();
        //head2
        if (checkPlace(Direction.NORTH, target, false, true, false)) return targetPos.up(2).north();
        if (checkPlace(Direction.SOUTH, target, false, true, false)) return targetPos.up(2).south();
        if (checkPlace(Direction.EAST, target, false, true, false)) return targetPos.up(2).east();
        if (checkPlace(Direction.WEST, target, false, true, false)) return targetPos.up(2).west();
       
        return null;
    }
    
    private boolean checkPlace(Direction direction, PlayerEntity target, boolean up, boolean up2, boolean down) {
        BlockPos headPos = up ? target.getBlockPos().up() : target.getBlockPos();
        BlockPos headPos2 = up2 ? target.getBlockPos().up(2) : target.getBlockPos();
        BlockPos downPos = down ? target.getBlockPos().down() : target.getBlockPos();
       
        if (mc.world.getBlockState(headPos).getMaterial().isReplaceable()
                && BlockUtils.canPlace(headPos.offset(direction))
                && (suicide.get() == true
                || (DamageUtils.bedDamage(target, Utils.vec3d(headPos)) >= minDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(headPos.offset(direction))) < maxSelfDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(headPos)) < maxSelfDamage.get()
                && abc(Utils.vec3d(headPos)) < maxFriendDamage.get()))) {
            this.direction = direction;
            return true;
        }
        if (mc.world.getBlockState(headPos2).getMaterial().isReplaceable()
        		&& !(BlockUtilsWorld.getBlock(target.getBlockPos().up()) instanceof BedBlock)
                && BlockUtils.canPlace(headPos2.offset(direction))
                && (suicide.get() == true
                || (DamageUtils.bedDamage(target, Utils.vec3d(headPos2)) >= minDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(headPos2.offset(direction))) < maxSelfDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(headPos2)) < maxSelfDamage.get()
                && abc(Utils.vec3d(headPos2)) < maxFriendDamage.get()))) {
        	 this.direction = direction;
             return true;
        }
        if (mc.world.getBlockState(downPos).getMaterial().isReplaceable()
                && BlockUtils.canPlace(downPos.offset(direction))
                && (suicide.get() == true
                || (DamageUtils.bedDamage(target, Utils.vec3d(downPos)) >= minDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(downPos.offset(direction))) < maxSelfDamage.get()
                && DamageUtils.bedDamage(mc.player, Utils.vec3d(downPos)) < maxSelfDamage.get()
                && abc(Utils.vec3d(downPos)) < maxFriendDamage.get()))) {
        	 this.direction = direction;
             return true;
        }

        return false;
    }
    
    private BlockPos doBreakPos() {
        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (!(blockEntity instanceof BedBlockEntity)) continue;

            BlockPos bedPos = blockEntity.getPos();
            Vec3d bedVec = Utils.vec3d(bedPos);

            if (PlayerUtils.distanceTo(bedVec) <= mc.interactionManager.getReachDistance()
                && DamageUtils.bedDamage(target, bedVec) >= minDamage.get()
                && DamageUtils.bedDamage(mc.player, bedVec) < maxSelfDamage.get()
                && abc(bedVec) < maxFriendDamage.get()
                
                && (suicide.get() || PlayerUtils.getTotalHealth() - DamageUtils.bedDamage(mc.player, bedVec) > 0)) {
                return bedPos;
            }
        }

        return null;
    }
    
    private boolean doPlace(BlockPos pos) {
        if (pos == null) return false;
        
        

        FindItemResult bed = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
        if (bed.getHand() == null && !autoSwitch.get()) return false;

        if (autoMove.get()) doAutoMove();
        
        double yaw;
        switch (d) {
            case East: {
                yaw = 90;
                break;
            }
            case South: {
                yaw = 180;
                break;
            }
            case West: {
                yaw = -90;
                break;
            }
            default: yaw = 0;
        }

        Rotations.rotate(yaw, Rotations.getPitch(pos), () -> {
            BlockUtils.place(pos, bed, false, 0, swing.get(), true);
            breakPos = pos;
        });

        return true;
    }
    
    private float yawFromDir(Direction direction) {
        switch (direction) {
            case EAST:  return 90;
            case NORTH: return 0;
            case SOUTH: return 180;
            case WEST:  return -90;
        }
        return 0;
    }
    
    private void doBreak(BlockPos pos) {
        if (pos == null) return;
        breakPos = null;

        if (!(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) return;

        boolean wasSneaking = mc.player.isSneaking();
        if (wasSneaking) mc.player.setSneaking(false);

        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.OFF_HAND, new BlockHitResult(mc.player.getPos(), Direction.UP, pos, false));

        mc.player.setSneaking(wasSneaking);
    }

    
    
    private void doAutoMove() {
        if (InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem).getSlot() == -1) {
            int slot = InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).getSlot();
            InvUtils.move().from(slot).toHotbar(autoMoveSlot.get() - 1);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render.get() && placePos != null && breakPos == null) {
            int x = placePos.getX();
            int y = placePos.getY();
            int z = placePos.getZ();

            switch (d) {
                case North: {
                    event.renderer.box(x, y, z, x + 1, y + 0.6, z + 2, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                    break;
                }
                case South: {
                    event.renderer.box(x, y, z - 1, x + 1, y + 0.6, z + 1, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                    break;
                }
                case East: {
                    event.renderer.box(x - 1, y, z, x + 1, y + 0.6, z + 1, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                    break;
                }
                case West: {
                    event.renderer.box(x, y, z, x + 2, y + 0.6, z + 1, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                    break;
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        if (target != null) return target.getEntityName();
        return null;
    }
    public static FindItemResult findPick() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem);
    }
    
    public static FindItemResult findSword() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof SwordItem);
    }
 
    public static boolean isWebbed(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        if ((BlockUtilsWorld.isAntiBed(pos) && BlockUtilsWorld.isAntiBed(pos.up())) || BlockUtilsWorld.isAntiBed(pos.up())) return true;
        return false;
    }
    
    public static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }
    
    private double abc(Vec3d pos)
    {
    	double BestDMG = 0;
    	for(PlayerEntity player : mc.world.getPlayers())
    	{
    		if(!Friends.get().isFriend(player)) continue;
    		double dmg = DamageUtils.bedDamage(player, pos);
    		if(dmg > BestDMG) BestDMG = dmg;
    	}
    	return BestDMG;
    	
    }
}