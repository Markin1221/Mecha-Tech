package com.markin.mechatech.mecha;

import java.util.List;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * Variante de uma peça (ex: Torso Blindado, Braço Broca) com atributos base e custo.
 * Atributos: peso, blindagem, força, velocidade, energia (EM/s), capacidade (só torso: peso máximo).
 */
public record PartVariant(String id, PartType type, int weight, int armor, int power, int speed, int energy,
						  int capacity, int time, List<Cost> cost) {

	public record Cost(Ingredient ingredient, int count) {}

	public static Cost cost(net.minecraft.world.level.ItemLike item, int count) {
		return new Cost(Ingredient.of(item), count);
	}
}
