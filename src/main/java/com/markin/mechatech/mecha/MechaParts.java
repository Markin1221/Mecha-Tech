package com.markin.mechatech.mecha;

import static com.markin.mechatech.mecha.PartVariant.cost;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.markin.mechatech.items.ModItems;

import net.minecraft.world.item.Items;

/** Lista de todas as variantes de peça que a Forja de Peças sabe fazer. */
public final class MechaParts {
	private static final Map<PartType, List<PartVariant>> BY_TYPE = new EnumMap<>(PartType.class);
	private static final Map<String, PartVariant> BY_ID = new HashMap<>();

	private MechaParts() {}

	private static void add(PartVariant v) {
		BY_TYPE.computeIfAbsent(v.type(), t -> new ArrayList<>()).add(v);
		BY_ID.put(v.id(), v);
	}

	public static List<PartVariant> of(PartType type) {
		return BY_TYPE.getOrDefault(type, List.of());
	}

	public static PartVariant byId(String id) {
		return BY_ID.get(id);
	}

	public static void initialize() {
		if (!BY_ID.isEmpty()) return;
		//            id                 tipo              peso blind força vel energ capac tempo
		// ===== CABEÇA =====
		add(new PartVariant("sensor_head", PartType.HEAD, 4, 3, 0, 0, 1, 0, 200, List.of(
			cost(ModItems.TITANIUM_PLATE, 4), cost(ModItems.BASIC_CIRCUIT, 1), cost(Items.GLASS, 2))));
		add(new PartVariant("radar_head", PartType.HEAD, 5, 3, 0, 0, 3, 0, 260, List.of(
			cost(ModItems.TITANIUM_PLATE, 4), cost(ModItems.BASIC_CIRCUIT, 2), cost(ModItems.NEODYMIUM_MAGNET, 2))));
		add(new PartVariant("thermal_head", PartType.HEAD, 5, 3, 0, 0, 3, 0, 260, List.of(
			cost(ModItems.TITANIUM_PLATE, 4), cost(ModItems.BASIC_CIRCUIT, 2), cost(ModItems.ETHER_CRYSTAL, 1))));
		// ===== TORSO =====
		add(new PartVariant("light_torso", PartType.TORSO, 20, 6, 0, 0, 2, 80, 400, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 6), cost(ModItems.TITANIUM_PLATE, 6), cost(ModItems.BASIC_CIRCUIT, 2))));
		add(new PartVariant("standard_torso", PartType.TORSO, 30, 10, 0, 0, 3, 120, 500, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 10), cost(ModItems.TITANIUM_PLATE, 8), cost(ModItems.SERVO_MOTOR, 2), cost(ModItems.BASIC_CIRCUIT, 2))));
		add(new PartVariant("armored_torso", PartType.TORSO, 45, 18, 0, 0, 4, 150, 600, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 10), cost(ModItems.RUBIC_COBALT_INGOT, 6), cost(ModItems.TITANIUM_PLATE, 8), cost(ModItems.BASIC_CIRCUIT, 2))));
		// ===== BRAÇO =====
		add(new PartVariant("fist_arm", PartType.ARM, 10, 4, 6, 0, 1, 0, 300, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 4), cost(ModItems.SERVO_MOTOR, 2), cost(ModItems.TITANIUM_PLATE, 4))));
		add(new PartVariant("drill_arm", PartType.ARM, 12, 3, 4, 0, 3, 0, 320, List.of(
			cost(ModItems.DRILL, 1), cost(ModItems.SERVO_MOTOR, 2), cost(ModItems.TITANIUM_PLATE, 4))));
		add(new PartVariant("saw_arm", PartType.ARM, 12, 3, 5, 0, 3, 0, 320, List.of(
			cost(ModItems.CHAINSAW, 1), cost(ModItems.SERVO_MOTOR, 2), cost(ModItems.TITANIUM_PLATE, 4))));
		add(new PartVariant("cannon_arm", PartType.ARM, 16, 4, 10, 0, 6, 0, 450, List.of(
			cost(ModItems.RUBIC_COBALT_INGOT, 4), cost(ModItems.BASIC_CIRCUIT, 3), cost(ModItems.SERVO_MOTOR, 1), cost(Items.TNT, 2))));
		add(new PartVariant("claw_arm", PartType.ARM, 9, 3, 3, 0, 2, 0, 280, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 3), cost(ModItems.SERVO_MOTOR, 3), cost(ModItems.NEODYMIUM_MAGNET, 2))));
		add(new PartVariant("shield_arm", PartType.ARM, 14, 12, 1, 0, 1, 0, 320, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 6), cost(ModItems.TITANIUM_PLATE, 6), cost(ModItems.SERVO_MOTOR, 1))));
		// ===== PERNAS =====
		add(new PartVariant("biped_legs", PartType.LEGS, 20, 6, 0, 6, 2, 0, 450, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 8), cost(ModItems.SERVO_MOTOR, 4), cost(ModItems.TITANIUM_PLATE, 6))));
		add(new PartVariant("tread_legs", PartType.LEGS, 26, 9, 0, 4, 3, 0, 500, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 10), cost(ModItems.SERVO_MOTOR, 3), cost(Items.IRON_BLOCK, 2))));
		add(new PartVariant("jumper_legs", PartType.LEGS, 18, 5, 0, 7, 3, 0, 480, List.of(
			cost(ModItems.TITANIUM_STEEL_INGOT, 6), cost(ModItems.SERVO_MOTOR, 4), cost(Items.PISTON, 4))));
		// ===== MOCHILA =====
		add(new PartVariant("jet_backpack", PartType.BACKPACK, 10, 2, 0, 2, 6, 0, 400, List.of(
			cost(ModItems.TITANIUM_PLATE, 6), cost(ModItems.BASIC_CIRCUIT, 2), cost(Items.BLAZE_ROD, 2))));
		add(new PartVariant("storage_backpack", PartType.BACKPACK, 8, 3, 0, 0, 0, 0, 300, List.of(
			cost(ModItems.TITANIUM_PLATE, 6), cost(Items.CHEST, 2))));
		add(new PartVariant("battery_backpack", PartType.BACKPACK, 12, 3, 0, 0, 0, 0, 400, List.of(
			cost(ModItems.TITANIUM_PLATE, 4), cost(ModItems.CONDUCTIVE_COPPER_INGOT, 6), cost(ModItems.BASIC_CIRCUIT, 2))));
	}
}
