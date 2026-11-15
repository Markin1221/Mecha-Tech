package com.markin.mechatech.block.entity;

import java.util.List;

import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaPartData;
import com.markin.mechatech.mecha.MechaParts;
import com.markin.mechatech.mecha.PartType;
import com.markin.mechatech.mecha.PartVariant;
import com.markin.mechatech.menu.PartForgeMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Forja de Peças (multibloco 2x2x2). Slots: 0..5 = materiais, 6 = peça pronta.
 * O jogador escolhe tipo, variante e escala; a forja gasta EM e materiais e cria a peça.
 */
public class PartForgeBlockEntity extends MachineBlockEntity implements MenuProvider {
	public static final int CAPACITY = 32000;
	public static final int INPUT = 512;
	public static final int USAGE = 24;
	public static final int OUTPUT_SLOT = 6;

	private int progress, progressTotal = 1;
	private int typeIndex, variantIndex, scale = 100;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 0 -> progress;
				case 1 -> progressTotal;
				case 2 -> (int) Math.min(Short.MAX_VALUE, energy.amount / 2);
				case 3 -> CAPACITY / 2;
				case 4 -> typeIndex;
				case 5 -> variantIndex;
				case 6 -> scale;
				case 7 -> 1;
				default -> 0;
			};
		}

		@Override
		public void set(int i, int v) {
			switch (i) {
				case 0 -> progress = v;
				case 1 -> progressTotal = v;
				case 2 -> energy.amount = v * 2L;
				case 4 -> typeIndex = v;
				case 5 -> variantIndex = v;
				case 6 -> scale = v;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 8;
		}
	};

	public PartForgeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PART_FORGE, pos, state, 7, CAPACITY, INPUT, 0);
	}


	// ===== Seleção (botões da tela) =====
	public PartVariant selected() {
		PartType type = PartType.values()[Math.floorMod(typeIndex, PartType.values().length)];
		List<PartVariant> list = MechaParts.of(type);
		return list.isEmpty() ? null : list.get(Math.floorMod(variantIndex, list.size()));
	}

	public void changeSelection(int button) {
		int types = PartType.values().length;
		switch (button) {
			case 0 -> { typeIndex = Math.floorMod(typeIndex - 1, types); variantIndex = 0; }
			case 1 -> { typeIndex = Math.floorMod(typeIndex + 1, types); variantIndex = 0; }
			case 2 -> variantIndex--;
			case 3 -> variantIndex++;
			case 4 -> scale = Math.max(MechaPartData.MIN_SCALE, scale - MechaPartData.STEP);
			case 5 -> scale = Math.min(MechaPartData.MAX_SCALE, scale + MechaPartData.STEP);
			default -> { return; }
		}
		PartVariant v = selected();
		if (v != null) variantIndex = MechaParts.of(v.type()).indexOf(v);
		progress = 0;
		setBuilding("", 0);
		setChanged();
	}

	// ===== Sincroniza com o cliente (pra animação) =====
	private String buildingVariant = "";
	private long buildStart;

	public String buildingVariant() { return buildingVariant; }
	public int buildScale() { return scale; }
	public long buildStart() { return buildStart; }
	public int buildTotal() { return progressTotal; }

	private void setBuilding(String variant, long start) {
		if (!variant.equals(buildingVariant) || (variant.isEmpty() != buildingVariant.isEmpty())) {
			buildingVariant = variant;
			buildStart = start;
			sync();
		}
	}

	private void sync() {
		setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}

	// ===== Tick =====
	public static final int BUILD_TIME = 140; // 7 segundos por peça (tempo da animação)

	public static void tick(Level level, BlockPos pos, BlockState state, PartForgeBlockEntity be) {
		boolean working = false;

		PartVariant v = be.selected();
		if (v != null && be.items.get(OUTPUT_SLOT).isEmpty() && be.hasMaterials(v)) {
			be.progressTotal = BUILD_TIME;
			if (be.energy.amount >= USAGE) {
				if (be.progress == 0 || be.buildingVariant.isEmpty()) be.setBuilding(v.id(), level.getGameTime() - be.progress);
				be.energy.amount -= USAGE;
				be.progress++;
				working = true;
				if (level instanceof ServerLevel sl && level.getGameTime() % 8 == 0) {
					sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 3, 0.4, 0.1, 0.4, 0.05);
				}
				if (be.progress >= be.progressTotal) {
					be.consumeMaterials(v);
					be.items.set(OUTPUT_SLOT, createPart(v, be.scale));
					be.progress = 0;
					be.setBuilding("", 0);
					level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.3f);
				}
				be.setChanged();
			}
		} else if (be.progress != 0 || !be.buildingVariant.isEmpty()) {
			be.progress = 0;
			be.setBuilding("", 0);
		}
		// Sem energia no meio: pausa a animação
		if (!working && !be.buildingVariant.isEmpty() && level.getGameTime() % 20 == 0) {
			be.buildStart = level.getGameTime() - be.progress;
			be.sync();
		}

		if (state.getValue(com.markin.mechatech.block.PartForgeBlock.LIT) != working) {
			level.setBlock(pos, state.setValue(com.markin.mechatech.block.PartForgeBlock.LIT, working), 3);
		}
	}

	public static ItemStack createPart(PartVariant v, int scale) {
		Item item = switch (v.type()) {
			case HEAD -> ModItems.MECHA_HEAD;
			case TORSO -> ModItems.MECHA_TORSO;
			case ARM -> ModItems.MECHA_ARM;
			case LEGS -> ModItems.MECHA_LEGS;
			case BACKPACK -> ModItems.MECHA_BACKPACK;
		};
		ItemStack stack = new ItemStack(item);
		stack.set(ModComponents.MECHA_PART, new MechaPartData(v.id(), scale));
		// Escolhe o modelo 3D certo da variante
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,
			new net.minecraft.world.item.component.CustomModelData(List.of(), List.of(), List.of(v.id()), List.of()));
		return stack;
	}

	/** Quantos itens do custo existem nos slots de material. */
	public int countOf(PartVariant.Cost cost) {
		int n = 0;
		for (int i = 0; i < OUTPUT_SLOT; i++) {
			ItemStack s = items.get(i);
			if (!s.isEmpty() && cost.ingredient().test(s)) n += s.getCount();
		}
		return n;
	}

	private boolean hasMaterials(PartVariant v) {
		for (PartVariant.Cost c : v.cost()) if (countOf(c) < c.count()) return false;
		return true;
	}

	private void consumeMaterials(PartVariant v) {
		for (PartVariant.Cost c : v.cost()) {
			int left = c.count();
			for (int i = 0; i < OUTPUT_SLOT && left > 0; i++) {
				ItemStack s = items.get(i);
				if (!s.isEmpty() && c.ingredient().test(s)) {
					int take = Math.min(left, s.getCount());
					s.shrink(take);
					left -= take;
				}
			}
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot != OUTPUT_SLOT;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Progress", progress);
		output.putString("Building", buildingVariant);
		output.putLong("BuildStart", buildStart);
		output.putInt("Type", typeIndex);
		output.putInt("Variant", variantIndex);
		output.putInt("Scale", scale);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		progress = input.getIntOr("Progress", 0);
		buildingVariant = input.getStringOr("Building", "");
		buildStart = input.getLongOr("BuildStart", 0L);
		typeIndex = input.getIntOr("Type", 0);
		variantIndex = input.getIntOr("Variant", 0);
		scale = input.getIntOr("Scale", 100);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mechatech.part_forge");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new PartForgeMenu(id, inventory, this, this.data, this);
	}
}
