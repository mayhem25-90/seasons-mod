package myseasons;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.world.WorldEvent;

public class SeasonsEventHandler implements ITickHandler {

    private final int TICKS = 20;
    private final int CHECK_INTERVAL = 2; // in seconds
    private final int DAY_LENGTH = 24000; // in ticks

    private int mTickCounter = 0;

    private SeasonsManager seasonsManager = new SeasonsManager();


    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // Functions
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // Main subscribe work function
    void onTick() {

        // Get world
        WorldServer world = DimensionManager.getWorld(0);
        if (world == null) return;

        // Every tick
        seasonsManager.setWorld(world);
//        seasonsManager.meltingSnowBase();

        // Every second
        if (mTickCounter % (TICKS) == 0);

        // Every <CHECK_INTERVAL> seconds
        if (mTickCounter % (CHECK_INTERVAL * TICKS) == 0) {
            checkSeason(world);
        }
    }


    void checkSeason(WorldServer world) {

        // Get number of day
        int dayNumber = (int) (world.getWorldTime() / DAY_LENGTH);

        // Current config: 1 day = 1 season
        int season = (dayNumber % SeasonsManager.SEASONS);
        System.out.println("# WorldTime " + world.getWorldTime() + "; day " + dayNumber + "; "
                + "season " + season + " (" + SeasonsManager.seasonSuffix[season] + ")");

        // Set season if changed
        if (seasonsManager.currentSeason != season) {
            seasonsManager.setSeason(season);
        }
    }

    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---


    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // ITickHandler
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.SERVER))) {
            onTick();
            mTickCounter++;
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return "";
    }
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
}
