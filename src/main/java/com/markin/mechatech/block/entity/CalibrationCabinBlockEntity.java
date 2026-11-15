package com.markin.mechatech.block.entity;

import java.util.List;

import com.markin.mechatech.block.CalibrationCabinBlock;
import com.markin.mechatech.exo.ExoArmorItem;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.menu.CalibrationCabinMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Cabine: guarda a Carcaça (slot 0) e EM; recarrega a exo de quem estiver dentro; controla a porta. */
public class CalibrationCabinBlockEntity extends MachineBlockEntity implements MenuProvider {
	public static final int CAPACITY = 64000;
	public static final int INPUT = 512;
	public static final int CHARGE_PER_SECOND = 4000; // dividido entre as peças
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private Vec3 lastOccupantPos = Vec3.ZERO;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 2 -> (int) Math.min(Short.MAX_VALUE, energy.amount / 2);  // sincroniza em "meio EM" (cabe em short)
				case 3 -> (int) (CAPACITY / 2);
				default -> 0;
			};
		}

		@Override
		public void set(int i, int v) {
			if (i == 2) energy.amount = v * 2L;
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public CalibrationCabinBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CALIBRATION_CABIN, pos, state, 1, CAPACITY, INPUT, 0);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CalibrationCabinBlockEntity be) {
		long time = level.getGameTime();
		if (time % 2 == 0) be.updateDoor(level, pos, state);
		if (time % 10 == 0 && be.energy.amount > 0) be.charge(level, pos);
	}

	// ===== Porta automática =====
	private void updateDoor(Level level, BlockPos pos, BlockState state) {
		List<Player> near = level.getEntitiesOfClass(Player.class, CalibrationCabinBlock.area(pos), p -> !p.isSpectator());
		Player closest = null;
		double best = Double.MAX_VALUE;
		for (Player p : near) {
			double d = p.distanceToSqr(pos.getX() + 0.5, p.getY(), pos.getZ() + 0.5);
			if (d < best) { best = d; closest = p; }
		}

		int door = state.getValue(CalibrationCabinBlock.DOOR);
		int open = state.getValue(CalibrationCabinBlock.OPEN);
		int targetDoor = door;
		boolean wantOpen = false;

		if (closest != null) {
			if (CalibrationCabinBlock.isInside(closest, pos)) {
				// Dentro: fecha quando parado; abre pra onde olha quando começa a andar
				Vec3 now = closest.position();
				boolean moving = now.distanceToSqr(lastOccupantPos) > 0.0004;
				lastOccupantPos = now;
				if (moving) {
					targetDoor = CalibrationCabinBlock.directionOfLook(closest.getYRot());
					wantOpen = true;
				}
			} else {
				// Fora e chegando perto: a porta vira pro jogador e abre
				targetDoor = CalibrationCabinBlock.directionTo(pos, closest.getX(), closest.getZ());
				wantOpen = true;
			}
		}

		int newDoor = door, newOpen = open;
		if (targetDoor != door) {
			// Precisa fechar antes de girar
			if (open > 0) newOpen = open - 1; else newDoor = targetDoor;
		} else if (wantOpen && open < 3) {
			newOpen = open + 1;
		} else if (!wantOpen && open > 0) {
			newOpen = open - 1;
		}

		if (newDoor != door || newOpen != open) {
			if (open == 0 && newOpen == 1) {
				level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.5f, 1.6f);
			} else if (open == 1 && newOpen == 0) {
				level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.5f, 1.6f);
			}
			BlockState s = state.setValue(CalibrationCabinBlock.DOOR, newDoor).setValue(CalibrationCabinBlock.OPEN, newOpen);
			level.setBlock(pos, s, 2);
			BlockState up = level.getBlockState(pos.above());
			if (up.is(state.getBlock())) {
				level.setBlock(pos.above(), up.setValue(CalibrationCabinBlock.DOOR, newDoor).setValue(CalibrationCabinBlock.OPEN, newOpen), 2);
			}
		}
	}

	// ===== Recarga (a cada 10 ticks) =====
	private void charge(Level level, BlockPos pos) {
		for (Player p : level.getEntitiesOfClass(Player.class, CalibrationCabinBlock.area(pos),
				pl -> CalibrationCabinBlock.isInside(pl, pos))) {
			int budget = (int) Math.min(CHARGE_PER_SECOND / 2, energy.amount);
			boolean charged = false;
			for (EquipmentSlot slot : SLOTS) {
				ItemStack piece = p.getItemBySlot(slot);
				if (!ExoArmorItem.isExo(piece) || budget <= 0) continue;
				int give = Math.min(Math.min(budget / 2 + 1, budget), ExoArmorItem.MAX_ENERGY - ExoArmorItem.getEnergy(piece));
				if (give <= 0) continue;
				ExoArmorItem.setEnergy(piece, ExoArmorItem.getEnergy(piece) + give);
				energy.amount -= give;
				budget -= give;
				charged = true;
			}
			if (charged) {
				setChanged();
				if (level instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.0, p.getZ(), 5, 0.3, 0.7, 0.3, 0.02);
				}
			}
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return stack.is(ModItems.EXO_FRAME);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mechatech.calibration_cabin");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new CalibrationCabinMenu(id, inventory, this, this.data, this.worldPosition);
	}
}
