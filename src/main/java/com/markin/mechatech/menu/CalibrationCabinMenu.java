package com.markin.mechatech.menu;

import com.markin.mechatech.block.CalibrationCabinBlock;
import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.exo.ExoArmorItem;
import com.markin.mechatech.exo.ExoModuleItem;
import com.markin.mechatech.items.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Menu da Cabine de Calibração.
 * Slots: 0 = Carcaça, 1..8 = módulos (2 por peça: capacete, peitoral, calças, botas), 9..44 = inventário.
 * Botões: 0 = montar exo-armadura, 1..16 = pintar (cor do corante), 17 = tirar pintura.
 */
public class CalibrationCabinMenu extends AbstractContainerMenu {
	public static final int BUTTON_ASSEMBLE = 0;
	public static final int BUTTON_PAINT_FIRST = 1;
	public static final int BUTTON_PAINT_CLEAR = 17;

	public static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
	private static final int MODULE_START = 1, MODULE_END = 9, INV_START = 9, INV_END = 45;

	private final Container frameContainer;
	private final ModuleContainer modules;
	private final ContainerData data;
	private final Player player;
	private final BlockPos pos;

	// Cliente
	public CalibrationCabinMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(1), new SimpleContainerData(4), null);
	}

	// Servidor
	public CalibrationCabinMenu(int id, Inventory inventory, Container frame, ContainerData data, BlockPos pos) {
		super(ModMenus.CALIBRATION_CABIN, id);
		this.frameContainer = frame;
		this.data = data;
		this.player = inventory.player;
		this.pos = pos;
		this.modules = new ModuleContainer(player, pos != null);

		this.addSlot(new Slot(frame, 0, 174, 22) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ModItems.EXO_FRAME);
			}
		});
		for (int piece = 0; piece < 4; piece++) {
			for (int k = 0; k < 2; k++) {
				this.addSlot(new ModuleSlot(modules, piece * 2 + k, 98 + k * 18, 22 + piece * 28, piece));
			}
		}
		this.addStandardInventorySlots(inventory, 28, 158);
		// Peças vestidas (escondidas): fazem a cor/energia atualizar na hora no cliente
		for (int i = 0; i < 4; i++) {
			this.addSlot(new HiddenSlot(inventory, 36 + i));
		}
		this.addDataSlots(data);
	}

	public int getCabinEnergy() { return data.get(2) * 2; }
	public int getCabinCapacity() { return Math.max(1, data.get(3) * 2); }

	public static EquipmentSlot slotOf(int piece) {
		return PIECES[piece].getSlot();
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == BUTTON_ASSEMBLE) return assemble(player);
		if (id >= BUTTON_PAINT_FIRST && id <= BUTTON_PAINT_CLEAR) return paint(player, id);
		return false;
	}

	private boolean assemble(Player player) {
		ItemStack frame = frameContainer.getItem(0);
		if (!frame.is(ModItems.EXO_FRAME)) return false;
		for (int i = 0; i < 4; i++) {
			if (ExoArmorItem.isExo(player.getItemBySlot(slotOf(i)))) {
				player.sendOverlayMessage(Component.translatable("message.mechatech.already_exo"));
				return false;
			}
		}
		Item[] items = {ModItems.EXO_HELMET, ModItems.EXO_CHESTPLATE, ModItems.EXO_LEGGINGS, ModItems.EXO_BOOTS};
		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = slotOf(i);
			ItemStack old = player.getItemBySlot(slot);
			if (!old.isEmpty() && !player.getInventory().add(old.copy())) {
				player.drop(old.copy(), false);
			}
			player.setItemSlot(slot, new ItemStack(items[i]));
		}
		frame.shrink(1);
		frameContainer.setChanged();
		modules.reload();
		player.level().playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1f, 0.8f);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1f, 1f);
		this.broadcastChanges();
		return true;
	}

	private boolean paint(Player player, int id) {
		Integer rgb = id == BUTTON_PAINT_CLEAR ? null
			: DyeColor.byId(id - BUTTON_PAINT_FIRST).getTextureDiffuseColor() & 0xFFFFFF;
		boolean any = applyPaint(player, rgb);
		if (any) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.DYE_USE, SoundSource.PLAYERS, 1f, 1f);
			player.sendOverlayMessage(rgb == null
				? Component.translatable("message.mechatech.paint_removed")
				: Component.translatable("message.mechatech.painted",
					Component.translatable("color.minecraft." + DyeColor.byId(id - BUTTON_PAINT_FIRST).getName())));
			// Garante que o cliente receba a armadura pintada na hora
			this.broadcastFullState();
			player.inventoryMenu.broadcastFullState();
		} else {
			player.sendOverlayMessage(Component.translatable("gui.mechatech.no_exo"));
		}
		return any;
	}

	/** Pinta (ou tira a tinta, se rgb == null) todas as peças da exo vestidas. */
	public static boolean applyPaint(Player player, Integer rgb) {
		boolean any = false;
		for (int i = 0; i < 4; i++) {
			ItemStack piece = player.getItemBySlot(slotOf(i));
			if (!ExoArmorItem.isExo(piece)) continue;
			if (rgb == null) piece.remove(DataComponents.DYED_COLOR);
			else piece.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
			any = true;
		}
		return any;
	}

	@Override
	public boolean stillValid(Player player) {
		return pos == null || CalibrationCabinBlock.isInside(player, pos);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		modules.save();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < INV_START) {
			if (!this.moveItemStackTo(stack, INV_START, INV_END, true)) return ItemStack.EMPTY;
		} else if (stack.is(ModItems.EXO_FRAME)) {
			if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
		} else if (stack.getItem() instanceof ExoModuleItem) {
			if (!this.moveItemStackTo(stack, MODULE_START, MODULE_END, false)) return ItemStack.EMPTY;
		} else {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}

	/** "Espelho" dos módulos que estão gravados nas peças vestidas. */
	private static class ModuleContainer extends SimpleContainer {
		private final Player player;
		private final boolean server;
		private boolean loading;

		ModuleContainer(Player player, boolean server) {
			super(8);
			this.player = player;
			this.server = server;
			reload();
		}

		void reload() {
			loading = true;
			for (int i = 0; i < 4; i++) {
				NonNullList<ItemStack> list = NonNullList.withSize(2, ItemStack.EMPTY);
				ItemStack piece = player.getItemBySlot(slotOf(i));
				if (ExoArmorItem.isExo(piece)) {
					piece.getOrDefault(ModComponents.EXO_MODULES, ItemContainerContents.EMPTY).copyInto(list);
				}
				setItem(i * 2, list.get(0));
				setItem(i * 2 + 1, list.get(1));
			}
			loading = false;
		}

		void save() {
			if (!server) return;
			for (int i = 0; i < 4; i++) {
				ItemStack piece = player.getItemBySlot(slotOf(i));
				if (!ExoArmorItem.isExo(piece)) continue;
				piece.set(ModComponents.EXO_MODULES,
					ItemContainerContents.fromItems(java.util.List.of(getItem(i * 2).copy(), getItem(i * 2 + 1).copy())));
			}
		}

		@Override
		public void setChanged() {
			super.setChanged();
			if (!loading) save();
		}
	}

	/** Slot invisível e travado: só serve pra sincronizar a armadura vestida. */
	private static class HiddenSlot extends Slot {
		HiddenSlot(Container c, int index) {
			super(c, index, -10000, -10000);
		}

		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public boolean mayPickup(Player player) {
			return false;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}

	/** Slot de módulo: só aceita módulo da peça certa, com a peça vestida, sem repetir. */
	private class ModuleSlot extends Slot {
		private final int piece;

		ModuleSlot(Container c, int index, int x, int y, int piece) {
			super(c, index, x, y);
			this.piece = piece;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			if (!(stack.getItem() instanceof ExoModuleItem m) || m.module.piece != PIECES[piece]) return false;
			if (!ExoArmorItem.isExo(player.getItemBySlot(slotOf(piece)))) return false;
			int other = getContainerSlot() % 2 == 0 ? getContainerSlot() + 1 : getContainerSlot() - 1;
			return !this.container.getItem(other).is(stack.getItem());
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public boolean isActive() {
			return true;
		}
	}
}
