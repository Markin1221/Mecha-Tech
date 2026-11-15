package com.markin.mechatech.block.entity;

import com.markin.mechatech.block.MechaAssemblerBlock;
import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.items.MechaItem;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;
import com.markin.mechatech.mecha.PartType;
import com.markin.mechatech.menu.MechaAssemblerMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Montadora de Mecha. Slots: 0 cabeça, 1 torso, 2 braço esq., 3 braço dir., 4 pernas, 5 mochila (opcional), 6 mecha pronto.
 * Monta peça por peça (7 s cada) e depois carrega o mecha que estiver na saída.
 */
public class MechaAssemblerBlockEntity extends MachineBlockEntity implements MenuProvider {
	public static final int CAPACITY = 200000, INPUT = 2048, USAGE = 40, PART_TIME = 140, CHARGE = 400;
	public static final int OUTPUT = 6;
	public static final PartType[] SLOT_TYPES = {PartType.HEAD, PartType.TORSO, PartType.ARM, PartType.ARM, PartType.LEGS, PartType.BACKPACK};

	// Status mostrado na tela
	public static final int IDLE = 0, MISSING = 1, TOO_BIG = 2, OVERWEIGHT = 3, OUTPUT_FULL = 4, NO_POWER = 5, BUILDING = 6, CHARGING = 7, READY = 8;

	private int progress, total = 1, status;
	private long buildStart;
	private int signature;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 0 -> progress;
				case 1 -> total;
				case 2 -> (int) Math.min(Short.MAX_VALUE, energy.amount / 10);
				case 3 -> CAPACITY / 10;
				case 4 -> status;
				case 5 -> size();
				default -> 0;
			};
		}

		@Override
		public void set(int i, int v) {
			switch (i) {
				case 0 -> progress = v;
				case 1 -> total = v;
				case 2 -> energy.amount = v * 10L;
				case 4 -> status = v;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 6;
		}
	};

	public MechaAssemblerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MECHA_ASSEMBLER, pos, state, 7, CAPACITY, INPUT, 0);
	}

	public int size() {
		return getBlockState().getBlock() instanceof MechaAssemblerBlock b ? b.size() : 3;
	}

	public Direction facing() {
		return getBlockState().getValue(MechaAssemblerBlock.FACING);
	}

	public static boolean accepts(int slot, ItemStack stack) {
		if (slot < 0 || slot >= 6) return false;
		MechaPartData d = stack.get(ModComponents.MECHA_PART);
		return d != null && d.def() != null && d.def().type() == SLOT_TYPES[slot];
	}

	private static MechaPartData part(ItemStack s) {
		MechaPartData d = s.get(ModComponents.MECHA_PART);
		return d == null || d.def() == null ? MechaData.NONE : d;
	}

	/** Mecha que está nos slots de entrada (pode estar incompleto). */
	public MechaData assembled() {
		return new MechaData(part(items.get(0)), part(items.get(1)), part(items.get(2)), part(items.get(3)),
			part(items.get(4)), part(items.get(5)), 0);
	}

	public int progress() { return progress; }
	public int total() { return total; }
	public long buildStart() { return buildStart; }
	public int status() { return status; }
	public ItemStack output() { return items.get(OUTPUT); }

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == OUTPUT ? stack.is(ModItems.MECHA) : accepts(slot, stack);
	}

	// ===== Tick =====
	public static void tick(Level level, BlockPos pos, BlockState state, MechaAssemblerBlockEntity be) {
		int old = be.status;
		MechaData d = be.assembled();
		ItemStack out = be.items.get(OUTPUT);
		boolean anyInput = false;
		for (int i = 0; i < 6; i++) anyInput |= !be.items.get(i).isEmpty();

		if (!anyInput) {
			be.progress = 0;
			be.status = out.isEmpty() ? IDLE : be.chargeOutput() ? CHARGING : READY;
		} else if (!d.complete()) {
			be.progress = 0; be.status = MISSING;
		} else if (d.scale() > MechaAssemblerBlock.maxScale(be.size())) {
			be.progress = 0; be.status = TOO_BIG;
		} else if (d.overweight()) {
			be.progress = 0; be.status = OVERWEIGHT;
		} else if (!out.isEmpty()) {
			be.progress = 0;
			be.status = be.chargeOutput() ? CHARGING : OUTPUT_FULL;
		} else {
			int parts = MechaData.present(d.backpack()) ? 6 : 5;
			be.total = parts * PART_TIME;
			if (be.energy.amount >= USAGE) {
				if (be.progress == 0 || be.status != BUILDING) be.buildStart = level.getGameTime() - be.progress;
				be.energy.amount -= USAGE;
				be.progress++;
				be.status = BUILDING;
				if (level instanceof ServerLevel sl) {
					if (be.progress % PART_TIME == PART_TIME - 1) {
						sl.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 0.8f);
					}
					if (level.getGameTime() % 6 == 0) {
						double[] c = be.center();
						sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, c[0], c[1] + d.height() * 0.5, c[2], 4, 0.6, d.height() * 0.3, 0.6, 0.1);
					}
				}
				if (be.progress >= be.total) be.finish(level, pos, d);
			} else {
				be.status = NO_POWER;
				be.buildStart = level.getGameTime() - be.progress;
			}
			be.setChanged();
		}

		// Sincroniza com o cliente quando muda algo visível
		int sig = be.status * 31 + (be.status == BUILDING ? 0 : be.progress);
		for (int i = 0; i < 7; i++) {
			ItemStack s = be.items.get(i);
			sig = sig * 31 + (s.isEmpty() ? 0 : s.getItem().hashCode() ^ java.util.Objects.hashCode(s.get(ModComponents.MECHA_PART)));
		}
		if (sig != be.signature || old != be.status) {
			be.signature = sig;
			be.sync();
		}
	}

	private void finish(Level level, BlockPos pos, MechaData d) {
		for (int i = 0; i < 6; i++) items.get(i).shrink(1);
		ItemStack mecha = new ItemStack(ModItems.MECHA);
		mecha.set(ModComponents.MECHA_DATA, d.withEnergy(0));
		items.set(OUTPUT, mecha);
		progress = 0;
		status = CHARGING;
		level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 0.8f);
	}

	/** Carrega o mecha da saída. Retorna true se ainda está carregando. */
	private boolean chargeOutput() {
		ItemStack out = items.get(OUTPUT);
		if (!out.is(ModItems.MECHA)) return false;
		MechaData d = MechaItem.data(out);
		if (d.energy() >= d.maxEnergy()) return false;
		int add = (int) Math.min(CHARGE, Math.min(energy.amount, d.maxEnergy() - d.energy()));
		if (add <= 0) return true;
		energy.amount -= add;
		out.set(ModComponents.MECHA_DATA, d.withEnergy(d.energy() + add));
		setChanged();
		return true;
	}

	/** Centro do piso da máquina (coordenadas do mundo). */
	public double[] center() {
		Direction r = MechaAssemblerBlock.right(facing()), b = MechaAssemblerBlock.back(facing());
		double h = size() / 2.0 - 0.5;
		return new double[]{worldPosition.getX() + 0.5 + h * (r.getStepX() + b.getStepX()), worldPosition.getY() + 0.2,
			worldPosition.getZ() + 0.5 + h * (r.getStepZ() + b.getStepZ())};
	}

	// ===== Sync =====
	private void sync() {
		setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Progress", progress);
		output.putInt("Total", total);
		output.putInt("Status", status);
		output.putLong("BuildStart", buildStart);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		progress = input.getIntOr("Progress", 0);
		total = input.getIntOr("Total", 1);
		status = input.getIntOr("Status", 0);
		buildStart = input.getLongOr("BuildStart", 0L);
	}

	// ===== Menu =====
	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mechatech.mecha_assembler", size());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new MechaAssemblerMenu(id, inventory, this, data);
	}
}
