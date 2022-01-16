package dextro.zeon.addon.modules.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dextro.zeon.addon.Zeon;
import dextro.zeon.addon.utils.Ezz;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;

public class ExtraNuker extends Module {
	
    public enum SortMode {
        None,
        Closest,
        Furthest
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final SettingGroup gsize = settings.createGroup("Size");

	private final Setting<Boolean> onlyOnGround = sgGeneral
		.add(new BoolSetting.Builder()
		.name("only-on-ground")
		.description("Works only when you standing on blocks.")
		.defaultValue(true)
		.build());

	public enum eType {
		None, Save, Replace
	}

	private final Setting<eType> itemsaver = sgGeneral
		.add(new EnumSetting.Builder<eType>()
		.name("item-saver")
		.description("Prevent destruction of tools.")
		.defaultValue(eType.Replace)
		.build());

	private final Setting<Boolean> sword = sgGeneral
		.add(new BoolSetting.Builder()
		.name("stop-on-sword")
		.description("Pause nuker if sword in main hand.")
		.defaultValue(true)
		.build());
	
	private final Setting<Boolean> checkchunk = sgGeneral
		.add(new BoolSetting.Builder()
		.name("chunk-border")
		.description("Break blocks in only current chunk.")
		.defaultValue(false)
		.build());
	
	private final Setting<Boolean> ignoreChests = sgGeneral
		.add(new BoolSetting.Builder()
		.name("ignore-chests")
		.description("Ignore chests and shulker box.")
		.defaultValue(true)
		.build());

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
    	.name("sort-mode")
    	.description("The blocks you want to mine first.")
    	.defaultValue(SortMode.Closest)
    	.build());
    
	private final Setting<Integer> spamlimit = sgGeneral
		.add(new IntSetting.Builder()
		.name("speed")
		.description("Block break speed.")
		.defaultValue(29)
		.min(1)
		.sliderMin(1)
		.sliderMax(100)
		.build());

	private final Setting<Double> lagg = sgGeneral
		.add(new DoubleSetting.Builder()
		.name("stop-on-lags")
		.description("Pause on server lagging. (Time since last tick)")
		.defaultValue(0.8)
		.min(0.1)
		.max(5)
		.sliderMin(0.1)
		.sliderMax(5)
		.build());

	private final Setting<Double> Distance = sgGeneral
		.add(new DoubleSetting.Builder()
		.name("distance")
		.description("Maximum distance.")
		.min(1)
		.defaultValue(6.6)
		.build());
	
	private final Setting<Boolean> onlySelected = sgGeneral
		.add(new BoolSetting.Builder()
		.name("only-selected")
		.description("Only mines your selected blocks.")
		.defaultValue(false)
		.build());

	private final Setting<List<Block>> selectedBlocks = sgGeneral
		.add(new BlockListSetting.Builder()
		.name("selected-blocks")
		.description("The certain type of blocks you want to mine.")
		.defaultValue(new ArrayList<>(0))
		.build());

	private final Setting<Integer> xmin = gsize.add(new IntSetting.Builder()
		.name("x-min")
		.defaultValue(1)
		.min(0)
		.max(6)
		.sliderMin(0)
		.sliderMax(6)
		.build());

	private final Setting<Integer> xmax = gsize.add(new IntSetting.Builder()
		.name("x-max")
		.defaultValue(1)
		.min(0)
		.max(6)
		.sliderMin(0)
		.sliderMax(6)
		.build());

	private final Setting<Integer> zmin = gsize.add(new IntSetting.Builder()
		.name("z-min")
		.defaultValue(1)
		.min(0)
		.max(6)
		.sliderMin(0)
		.sliderMax(6)
		.build());

	private final Setting<Integer> zmax = gsize.add(new IntSetting.Builder()
		.name("z-max")
		.defaultValue(1)
		.min(0)
		.max(6)
		.sliderMin(0)
		.sliderMax(6)
		.build());

	private final Setting<Integer> ymin = gsize.add(new IntSetting.Builder()
		.name("up")
		.defaultValue(1)
		.min(1)
		.max(6)
		.sliderMin(1)
		.sliderMax(6)
		.build());

	private final Setting<Integer> ymax = gsize.add(new IntSetting.Builder()
		.name("down")
		.defaultValue(0)
		.min(0)
		.max(7)
		.sliderMin(0)
		.sliderMax(7)
		.build());


	public ExtraNuker() {
		super(Zeon.Misc, "Nuker+", "Breaks a large amount of specified blocks around you.");
	}

	int limit = 0;
	byte pause = 0;
    private final List<BlockPos> blocks = new ArrayList<>();


    @Override
    public void onActivate() {
    	
		limit = 0;
		pause = 0;
    }


	@EventHandler(priority = Integer.MIN_VALUE)
	private void ADD_LIMIT(PacketEvent.Send e) {
		if (!e.isCancelled()) limit++;
	}

	@EventHandler
	private void onTick(TickEvent.Pre event) {

		try {
			
			blocks.clear();
			
			if (pause > 0) {pause--; return;}

			if (onlyOnGround.get() && !mc.player.isOnGround()) return;

			if (TickRate.INSTANCE.getTimeSinceLastTick() >= lagg.get()) return;

			if (sword.get() && mc.player.getMainHandStack().getItem() instanceof SwordItem) return;

			limit = 0;

			int px = mc.player.getBlockPos().getX();
			int py = mc.player.getBlockPos().getY();
			int pz = mc.player.getBlockPos().getZ();

			for (int x = px - xmin.get(); x <= px + xmax.get(); x++) {
				for (int z = pz - zmin.get(); z <= pz + zmax.get(); z++) {
					for (int y = py - ymax.get(); y <= py + ymin.get() - 1; y++) {

						BlockPos pos = new BlockPos(x, y, z);
						Block b = mc.world.getBlockState(pos).getBlock();
						
						if (checkchunk.get() && (mc.world.getChunk(pos).getPos() != mc.world.getChunk(mc.player.getBlockPos()).getPos()) ) continue;
						if (mc.world.getBlockState(pos).getOutlineShape(mc.world, pos) == VoxelShapes.empty()) continue;
						if (b == Blocks.BEDROCK) continue;
						if (distance(pos.getX(), pos.getY(), pos.getZ()) >= Distance.get()) continue;
						if (onlySelected.get() && !selectedBlocks.get().contains(b)) continue;
						if(ignoreChests.get() && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.ENDER_CHEST || b == Blocks.SHULKER_BOX || b.toString().contains("_shulker_box")) ) continue;
						
						blocks.add(pos);
						
					}
				}
			}

			
			double pX = mc.player.getX() - 0.5;
			double pY = mc.player.getY();
			double pZ = mc.player.getZ() - 0.5;

			if (sortMode.get() != SortMode.None) {
				blocks.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, value.getX(), value.getY(), value.getZ()) * (sortMode.get() == SortMode.Closest ? 1 : -1)));
			}


			switch (itemsaver.get()) {
			case None:
				break;
			case Save:
				if (isbreak()) {
					toggle();
					return;
				}
			case Replace:
				if (isbreak()) {
					if (swap_item()) {
						pause = 5;
						return;
					} else {
						toggle();
						return;
					}
				}
			}

			for(int q=0; q < blocks.size(); q++){
				if (limit > spamlimit.get()) return;
				mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blocks.get(q), Direction.UP));
				mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blocks.get(q), Direction.UP));
			}
			
		} catch (Exception ignored) {}
	}

	private boolean isbreak() {
		if (mc.player.getMainHandStack().getDamage() != 0 && mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage() < 31) return true;
		return false;
	}

	private boolean swap_item() {
		Item item = mc.player.getMainHandStack().getItem();
		for (int x = 0; x < mc.player.getInventory().size(); x++) {
			if (mc.player.getInventory().getStack(x).getItem() != item) continue;
			if (mc.player.getInventory().getStack(x).getMaxDamage() - mc.player.getInventory().getStack(x).getDamage() < 31) continue;
			Ezz.clickSlot(Ezz.invIndexToSlotId(x),mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
			return true;
		}
		return false;
	}

	private double distance(double x, double y, double z) {
		if (x > 0) {x = x + 0.5;} else {x = x - 0.5;}
		if (y > 0) {y = y + 0.5;} else {y = y - 0.5;}
		if (z > 0) {z = z + 0.5;} else {z = z - 0.5;}
		double d = mc.player.getPos().getX() - x;
		if (d < 0) d--;
		double e = mc.player.getPos().getY() + 1 - y;
		double f = mc.player.getPos().getZ() - z;
		if (f < 0) f--;

		return Math.sqrt(d * d + e * e + f * f);
	}

}
