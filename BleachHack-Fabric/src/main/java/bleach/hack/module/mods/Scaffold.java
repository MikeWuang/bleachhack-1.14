package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket.Mode;
import net.minecraft.server.network.packet.PlayerInteractBlockC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 1.5, 0.3, 1, "Range: "));
	
	private HashMap<BlockPos, Integer> lastPlaced = new HashMap<>();
	
	public Scaffold() {
		super("Scaffold", GLFW.GLFW_KEY_N, Category.PLAYER, "Places blocks under you", settings);
	}
	
	public void onUpdate() {
		HashMap<BlockPos, Integer> tempMap = new HashMap<>();
		for(Entry<BlockPos, Integer> e: lastPlaced.entrySet()) {
			if(e.getValue() > 0) tempMap.put(e.getKey(), e.getValue() - 1);
		}
		lastPlaced.clear();
		lastPlaced.putAll(tempMap);
		
		if(!(mc.player.inventory.getMainHandStack().getItem() instanceof BlockItem)) return;
		
		double range = getSettings().get(0).toSlider().getValue();
		for(int r = 0; r < 5; r++) {
			Vec3d r1 = new Vec3d(0,-0.85,0);
			if(r == 1) r1 = r1.add(range, 0, 0);
			if(r == 2) r1 = r1.add(-range, 0, 0);
			if(r == 3) r1 = r1.add(0, 0, range);
			if(r == 4) r1 = r1.add(0, 0, -range);
			
			if(WorldUtils.NONSOLID_BLOCKS.contains(
					mc.world.getBlockState(new BlockPos(mc.player.getPos().add(r1))).getBlock())) {
				placeBlockAuto(new BlockPos(mc.player.getPos().add(r1)));
				return;
			}
		}
	}
	
	public void placeBlockAuto(BlockPos block) {
		if(lastPlaced.containsKey(block)) return;
		for(Direction d: Direction.values()) {
			if(!WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
				if(WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SNEAKING));}
				mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
						new BlockHitResult(new Vec3d(block), d.getOpposite(), block.offset(d), false)));
				mc.player.swingHand(Hand.MAIN_HAND);
				mc.world.playSound(block, SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.BLOCKS, 1f, 1f, false);
				if(WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SNEAKING));}
				lastPlaced.put(block, 5);
				return;
			}
		}
	}

}