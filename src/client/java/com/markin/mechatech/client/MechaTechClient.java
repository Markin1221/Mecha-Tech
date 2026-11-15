package com.markin.mechatech.client;

import com.markin.mechatech.client.screen.AlloySmelterScreen;
import com.markin.mechatech.client.screen.CombustionGeneratorScreen;
import com.markin.mechatech.client.screen.MechanicalWorkbenchScreen;
import com.markin.mechatech.menu.ModMenus;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MechaTechClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.MECHANICAL_WORKBENCH, MechanicalWorkbenchScreen::new);
		MenuScreens.register(ModMenus.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
		MenuScreens.register(ModMenus.ALLOY_SMELTER, AlloySmelterScreen::new);
	}
}
