/*
 * Copyright 2023 FrozenBlock
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

package net.frozenblock.wilderwild.mixin.server.general;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Goat.class)
public class GoatMixin {

	@Unique
	private boolean wilderWild$isTreetrain1() {
		Goat goat = Goat.class.cast(this);
		String string = ChatFormatting.stripFormatting(goat.getName().getString());
		return Objects.equals(string, "Treetrain1");
	}

	@Inject(method = "isScreamingGoat", at = @At("RETURN"), cancellable = true)
	private void wilderWild$isScreamingGoat(CallbackInfoReturnable<Boolean> info) {
		if (this.wilderWild$isTreetrain1()) {
			info.setReturnValue(true);
		}
	}

}
