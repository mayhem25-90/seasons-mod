package myseasons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = MainClass.MODID, name = MainClass.MODNAME, version = MainClass.MODVERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)

public class MainClass {

	public static final String MODID = "myseasonsmod";
	public static final String MODNAME = "Mayhem Seasons Mod";
	public static final String MODVERSION = "0.0.1";

	SeasonsEventHandler evtHandler;

	// - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
	// Standard initial functions
	// - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}

	@EventHandler
	public void Init(FMLPreInitializationEvent event) {
	}

	@EventHandler
	public void postInit(FMLPreInitializationEvent event) {
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		evtHandler = new SeasonsEventHandler();

		TickRegistry.registerTickHandler(evtHandler, Side.SERVER);
		MinecraftForge.EVENT_BUS.register(evtHandler);
	}
}
