package dextro.zeon.addon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dextro.zeon.addon.modules.Combat.BedAuraPlus;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

public class BlockUtilsWorld {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isTrapBlock(BlockPos pos) {
        return getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.BEDROCK;
    }
    
    public static Block getBlock(BlockPos p) {
        if (p == null) return null;
        return mc.world.getBlockState(p).getBlock();
    }
    
    public static boolean isBurrowed(PlayerEntity p) {
    	if(p == null) return false;
    	if(mc.world == null) return false;
    	return mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.OBSIDIAN;
    }
    
    public static boolean isBurrowedLiving(LivingEntity p) {
    	if(p == null) return false;
    	if(mc.world == null) return false;
    	return mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.OBSIDIAN;
    }

    public static void mineWeb(PlayerEntity p, int swordSlot) {
        if (p == null || swordSlot == -1) return;
        BlockPos pos = p.getBlockPos();
        BlockPos webPos = null;
        if (isAntiBed(pos)) webPos = pos;
        if (isAntiBed(pos.up())) webPos = pos.up();
        if (isAntiBed(pos.up(2))) webPos = pos.up(2);
        if (webPos == null) return;
        InvUtils.swap(swordSlot, false);
        Mine(webPos, swordSlot);
    }
    
    public static boolean isAntiBed(BlockPos pos) {
        return BlockUtilsWorld.getBlock(pos) == Blocks.COBWEB || BlockUtilsWorld.getBlock(pos) == Block.getBlockFromItem(Items.STRING);
    }
    

    public static ArrayList<Vec3d> surround = new ArrayList<Vec3d>() {{
        add(new Vec3d(1, 0, 0));
        add(new Vec3d(-1, 0, 0));
        add(new Vec3d(0, 0, 1));
        add(new Vec3d(0, 0, -1));
    }};
    

    public static ArrayList<Vec3d> selfTrap = new ArrayList<Vec3d>() {{
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};
    

    public static ArrayList<Vec3d> head = new ArrayList<Vec3d>() {{
        add(new Vec3d(0, 2, 0));
    }};

    public static boolean isTrapped(PlayerEntity p) {
    	BlockPos south = p.getBlockPos().up().south();
    	BlockPos north = p.getBlockPos().up().north();
    	BlockPos east = p.getBlockPos().up().east();
    	BlockPos west = p.getBlockPos().up().west();
    	BlockPos head = p.getBlockPos().up(2);
    	
    	if(isTrapBlock(south) && isTrapBlock(north) && isTrapBlock(east)&& isTrapBlock(west)&&isTrapBlock(head))return true;
    	return false;
    }
    
    public static boolean isSurrounded(PlayerEntity p) {
    	BlockPos south = p.getBlockPos().south();
    	BlockPos north = p.getBlockPos().north();
    	BlockPos east = p.getBlockPos().east();
    	BlockPos west = p.getBlockPos().west();
    	
    	if(isTrapBlock(south) && isTrapBlock(north) && isTrapBlock(east)&& isTrapBlock(west))return true;
    	return false;
    		
    }
    
    
    public static BlockPos LowestDist(PlayerEntity a) {
    	BlockPos south = a.getBlockPos().up(2).south();
    	BlockPos north = a.getBlockPos().up(2).north();
    	BlockPos east = a.getBlockPos().up(2).east();
    	BlockPos west = a.getBlockPos().up(2).west();
    	
    	double south1 = distanceBetween(mc.player.getBlockPos().up(), south);
    	double north1 = distanceBetween(mc.player.getBlockPos().up(), north);
    	double east1 = distanceBetween(mc.player.getBlockPos().up(), east);
    	double west1 = distanceBetween(mc.player.getBlockPos().up(), west);
    	
    	if(south1 < north1 && south1 < east1 && south1 < west1 && south1 <= 6 && mc.world.getBlockState(south).getMaterial().isReplaceable()) return south;
    	if(east1 < north1 && east1 < south1 && east1 < west1 && south1 <= 6 && mc.world.getBlockState(east).getMaterial().isReplaceable()) return east;
    	if(north1 < south1 && north1 < east1 && north1 < west1 && north1 <= 6 && mc.world.getBlockState(north).getMaterial().isReplaceable()) return north;
    	if(west1 < north1 && west1 < south1 && west1 < east1 && west1 <= 6 && mc.world.getBlockState(west).getMaterial().isReplaceable()) return west;
    	return null;
    }
    
    public static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }
   
    public static int distance(BlockPos first, BlockPos second) {
		return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ());
	}
    
    public static void Mine(BlockPos targetPos, int slot) {
    	InvUtils.swap(slot, false);
        mc.player.networkHandler.sendPacket((Packet<?>) new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        InvUtils.swap(slot, false);
        mc.player.networkHandler.sendPacket((Packet<?>) new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }
    
    public static BlockPos getSelf(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList<>();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) return tpos.up(2);
        for (Vec3d stp : selfTrap) {
            BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
            if (isTrapBlock(stb)) selfTrapBlocks.add(stb);
        }
        if (selfTrapBlocks.isEmpty()) return null;
        return selfTrapBlocks.get(new Random().nextInt(selfTrapBlocks.size()));
    }

    public static BlockPos getHead(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList<>();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) return tpos.up(2);
        for (Vec3d stp : head) {
            BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
            if (isTrapBlock(stb)) selfTrapBlocks.add(stb);
        }
        if (selfTrapBlocks.isEmpty()) return null;
        return selfTrapBlocks.get(new Random().nextInt(selfTrapBlocks.size()));
    }
    
    public static BlockPos getSurr(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList<>();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) return tpos.up(2);
        for (Vec3d stp : surround) {
            BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
            if (isTrapBlock(stb)) selfTrapBlocks.add(stb);
        }
        if (selfTrapBlocks.isEmpty()) return null;
        return selfTrapBlocks.get(new Random().nextInt(selfTrapBlocks.size()));
    }
    
    public static double distanceTo(BlockPos pos){
        return distanceTo(pos.getX(), pos.getY(), pos.getZ());
    }

    public static double distanceTo(double x, double y, double z){
        if(x >= 0) x += 0.5; else x -= 0.5;
        if(y >= 0) y += 0.5; else y -= 0.5;
        if(z >= 0) z += 0.5; else z -= 0.5;
        double px = mc.player.getX();
        double py = mc.player.getY() + 1.25;
        double pz = mc.player.getZ();
        if(px < 0) px--;
        if(pz < 0) pz--;
        double f = px - x;
        double g = py - y;
        double h = pz - z;
        return Math.sqrt(f * f + g * g + h * h);
    }



    private static final SetBlockResult RESULT = new SetBlockResult();

    public static class SetBlockResult {
        private static int slot = -1;
        private static BlockPos pos = null;
        private static Direction direct = Direction.DOWN;
        private static boolean rotate = false;
        private static boolean noback = false;
        private static boolean packet = false;
        private static Hand hand = Hand.MAIN_HAND;

        public SetBlockResult POS(BlockPos s) {
            pos = s;
            return this;
        }
        public SetBlockResult DIRECTION(Direction s) {
            direct = s;
            return this;
        }
        public SetBlockResult ROTATE(boolean s) {
            rotate = s;
            return this;
        }
        public SetBlockResult XYZ(int x, int y, int z) {
            pos = new BlockPos(x,y,z);
            return this;
        }
        public SetBlockResult RELATIVE_XYZ(int x, int y, int z) {
            pos = new BlockPos(mc.player.getBlockPos().getX() + x, mc.player.getBlockPos().getY() + y, mc.player.getBlockPos().getZ() + z);
            return this;
        }
        public SetBlockResult NOBACK() {
            noback = true;
            return this;
        }
        public SetBlockResult PACKET(boolean s) {
            packet = s;
            return this;
        }
        public SetBlockResult SLOT(int slot) {
            this.slot = slot;
            return this;
        }
        public SetBlockResult INDEX_SLOT(int s) {
            slot = invIndexToSlotId(s);
            return this;
        }
        public SetBlockResult HAND(Hand hand) {
            this.hand = hand;
            return this;
        }
        private void reset() {
            slot = -1;
            pos = null;
            direct = Direction.DOWN;
            rotate = false;
            noback = false;
            packet = false;
        }

        public boolean S() {
            if( pos == null
                || slot == -1
                || mc.player.getInventory().getStack(slot).isEmpty()
                || !(mc.player.getInventory().getStack(slot).getItem() instanceof BlockItem) ) {
                reset();
                return false;
            }


            if (!BlockUtils.canPlace(pos, true)) {
                reset();
                return false;
            }

            Block block = ((BlockItem) mc.player.getInventory().getStack(slot).getItem()).getBlock();
            if(!block.canPlaceAt(block.getDefaultState(), mc.world, pos)) {
                reset();
                return false;
            }

            int PreSlot = mc.player.getInventory().selectedSlot;
            swap(slot);

            if(rotate){
                Vec3d hitPos = new Vec3d(0, 0, 0);
                ((IVec3d) hitPos).set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos));
            }
            BlockHitResult hitresult = new BlockHitResult(mc.player.getPos(), direct, pos, true);

            if(packet) mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hitresult));
            else mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitresult);

            if(!noback) swap(PreSlot);

            reset();

            return true;
        }

    }

    public static SetBlockResult setBlock(){
        return RESULT;
    }


    public static int invIndexToSlotId(int invIndex) {
        if (invIndex < 9 && invIndex != -1) return 44 - (8 - invIndex);
        return invIndex;
    }



    public static void swap(int s) {
        if (s != mc.player.getInventory().selectedSlot && s >= 0 && s < 9) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(s));
            mc.player.getInventory().selectedSlot = s;
        }
    }
    
    public static boolean isBlastResistant(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().getBlastResistance() >= 600;
    }
    
    public static boolean isSurroundBroken(LivingEntity targetEntity) {
        assert mc.world != null;
        return (!isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && !isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)));
    }
    
    public static boolean inFace(LivingEntity target) {
        assert mc.world != null;
        return isBlastResistant(target.getBlockPos().add(1, 1, 0)) && isBlastResistant(target.getBlockPos().add(-1, 1, 0)) && isBlastResistant(target.getBlockPos().add(0, 1, 1)) && isBlastResistant(target.getBlockPos().add(0, 1, -1));
    }

    public static void clickSlot(int slot, int button, SlotActionType action) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, button, action, mc.player);
    }
    
    public static float startYaw, startPitch;
    
    public static boolean isSurrounded(LivingEntity targetEntity) {
        assert mc.world != null;
        return isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1));
    }
    
    private static final Vec3d eyesPos = new Vec3d(mc.player.getX(),
            mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
            mc.player.getZ());
    
    public static void rotateBl(BlockPos bp) {
        startYaw = mc.player.getYaw();
        startPitch = mc.player.getPitch();
        Vec3d vec = new Vec3d(bp.getX(), bp.getY(), bp.getZ());
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float[] rotations = {
                mc.player.getYaw()
                        + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
                mc.player.getPitch() + MathHelper
                        .wrapDegrees(pitch - mc.player.getPitch())};

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround()));
    }

    public static boolean isGreenHole(LivingEntity target) {
        assert mc.world != null;
        return mc.world.getBlockState(target.getBlockPos().add(1, 0, 0)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(-1, 0, 0)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(0, 0, 1)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(0, 0, -1)).isOf(Blocks.BEDROCK);
    }
    
    public static float getTotalHealthLiving(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }
    
    public static boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }
    

    public static boolean isTopTrapped(LivingEntity target) {
        assert mc.world != null;
        return isBlastResistant(target.getBlockPos().add(0, 2, 0));
    }
    
    public static void rotateEnt(Entity e) {
        startYaw = mc.player.getYaw();
        startPitch = mc.player.getPitch();
        Vec3d vec = new Vec3d(e.getX(), e.getY(), e.getZ());
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float[] rotations = {
                mc.player.getYaw()
                        + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
                mc.player.getPitch() + MathHelper
                        .wrapDegrees(pitch - mc.player.getPitch())};

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1],mc.player.isOnGround()));
    }
    
    public static float transformForDifficulty(float f) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) f = 0.0F;
        if (mc.world.getDifficulty() == Difficulty.EASY) f = Math.min(f / 2.0F + 1.0F, f);
        if (mc.world.getDifficulty() == Difficulty.HARD) f = f * 3.0F / 2.0F;
        return f;
    }
    
	public static void rotate(Vec3d vec, boolean sendPacket) {
        float[] rotations = getRotations(vec);
		
        if (sendPacket) mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround()));
         rotations[0] = mc.player.getYaw();
         rotations[1] = mc.player.getPitch();
	}
	
	public static float[] getRotations(Vec3d vec) {
		Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
		double diffX = vec.x - eyesPos.x;
		double diffY = vec.y - eyesPos.y;
		double diffZ = vec.z - eyesPos.z;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[]{mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()), mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())};
	}
	
	public static void BlockRotate(BlockPos blockPos) {
		Vec3d hitPos = new Vec3d(0, 0, 0);
        ((IVec3d) hitPos).set(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        Rotations.getYaw(hitPos);
        Rotations.getPitch(hitPos);
	}
}


