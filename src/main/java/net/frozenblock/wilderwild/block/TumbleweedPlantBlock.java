/*
 * Copyright 2023-2024 FrozenBlock
 * This file is part of Wilder Wild.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

package net.frozenblock.wilderwild.block;

import com.mojang.serialization.MapCodec;
import net.frozenblock.wilderwild.entity.Tumbleweed;
import net.frozenblock.wilderwild.registry.RegisterBlocks;
import net.frozenblock.wilderwild.registry.RegisterEntities;
import net.frozenblock.wilderwild.registry.RegisterSounds;
import net.frozenblock.wilderwild.tag.WilderBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TumbleweedPlantBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<TumbleweedPlantBlock> CODEC = simpleCodec(TumbleweedPlantBlock::new);
	public static final int MAX_AGE = 3;
	public static final int AGE_FOR_SOLID_COLLISION = 2;
	public static final int SNAP_CHANCE = 4;
	public static final int REPRODUCTION_CHANCE_PEACEFUL = 20;
	public static final int REPRODUCTION_CHANCE_DIVIDER_BY_DIFFICULTY = 15;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape[] SHAPES = new VoxelShape[]{
		Block.box(3, 0, 3, 12, 9, 12),
		Block.box(2, 0, 2, 14, 12, 14),
		Block.box(1, 0, 1, 15, 14, 15),
		Block.box(1, 0, 1, 15, 14, 15)
	};

	public TumbleweedPlantBlock(@NotNull BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static boolean isFullyGrown(@NotNull BlockState state) {
		return state.getValue(AGE) == MAX_AGE;
	}

	@NotNull
	@Override
	protected MapCodec<? extends TumbleweedPlantBlock> codec() {
		return CODEC;
	}

	@Override
	public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
		if (isFullyGrown(state)) {
			if (random.nextInt(SNAP_CHANCE) == 0) {
				level.setBlock(pos, state.cycle(AGE), UPDATE_CLIENTS);
				Tumbleweed weed = new Tumbleweed(RegisterEntities.TUMBLEWEED, level);
				level.addFreshEntity(weed);
				weed.setPos(Vec3.atBottomCenterOf(pos));
				int diff = level.getDifficulty().getId();
				if (level.getRandom().nextInt(diff == 0 ? REPRODUCTION_CHANCE_PEACEFUL : (REPRODUCTION_CHANCE_DIVIDER_BY_DIFFICULTY / diff)) == 0) {
					weed.setItem(new ItemStack(RegisterBlocks.TUMBLEWEED_PLANT), true);
				}
				level.playSound(null, pos, RegisterSounds.ENTITY_TUMBLEWEED_DAMAGE, SoundSource.BLOCKS, 1F, 1F);
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
			}
		} else {
			level.setBlock(pos, state.cycle(AGE), UPDATE_CLIENTS);
		}
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
		return blockState.getValue(AGE) < AGE_FOR_SOLID_COLLISION ? Shapes.empty() : super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPES[state.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return state.is(BlockTags.DEAD_BUSH_MAY_PLACE_ON) || state.is(WilderBlockTags.BUSH_MAY_PLACE_ON) || super.mayPlaceOn(state, level, pos);
	}

	@Override
	public boolean isValidBonemealTarget(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
		return !isFullyGrown(state);
	}

	@Override
	public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
		level.setBlockAndUpdate(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + random.nextIntBetweenInclusive(1, 2))));
	}

	@NotNull
	@Override
	public ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (stack.is(Items.SHEARS) && isFullyGrown(state)) {
			if (!level.isClientSide) {
				level.playSound(null, pos, RegisterSounds.BLOCK_TUMBLEWEED_SHEAR, SoundSource.BLOCKS, 1F, 1F);
				Tumbleweed weed = new Tumbleweed(RegisterEntities.TUMBLEWEED, level);
				level.addFreshEntity(weed);
				weed.setPos(Vec3.atBottomCenterOf(pos));
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
				weed.spawnedFromShears = true;
				level.setBlockAndUpdate(pos, state.setValue(AGE, 0));
				stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
				level.gameEvent(player, GameEvent.SHEAR, pos);
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}
}
