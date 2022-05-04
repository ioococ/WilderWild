package net.frozenblock.wilderwild.mixin;

import net.frozenblock.wilderwild.block.BlockSoundGroupOverwrites;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

	@Inject(method = "getSoundGroup", at = @At("TAIL"), cancellable = true)
	private void getSoundGroupMixin(BlockState state, CallbackInfoReturnable<BlockSoundGroup> info){
		Block block = state.getBlock();
		Identifier id = Registry.BLOCK.getId(block);
		if (BlockSoundGroupOverwrites.ids.contains(id)) {
			int index = BlockSoundGroupOverwrites.ids.indexOf(id);
			info.setReturnValue(BlockSoundGroupOverwrites.soundGroups.get(index));
			info.cancel();
		}
	}
}