package com.markin.mechatech.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/** Base das máquinas: inventário + energia salvos no mundo. */
public abstract class MachineBlockEntity extends BlockEntity implements Container {
	protected final NonNullList<ItemStack> items;
	public final SimpleEnergyStorage energy;

	protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots,
								 long capacity, long maxInsert, long maxExtract) {
		super(type, pos, state);
		this.items = NonNullList.withSize(slots, ItemStack.EMPTY);
		this.energy = new SimpleEnergyStorage(capacity, maxInsert, maxExtract) {
			@Override
			protected void onFinalCommit() {
				setChanged();
			}
		};
	}

	// ===== Salvar / carregar =====
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putLong("Energy", this.energy.amount);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.replaceAll(s -> ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.energy.amount = Math.min(input.getLongOr("Energy", 0L), this.energy.capacity);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null) {
			Containers.dropContents(this.level, pos, this);
		}
	}

	// ===== Container =====
	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack s : this.items) if (!s.isEmpty()) return false;
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack r = ContainerHelper.removeItem(this.items, slot, amount);
		if (!r.isEmpty()) setChanged();
		return r;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}
}
