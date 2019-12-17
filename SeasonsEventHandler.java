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
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.world.WorldEvent;

public class SeasonsEventHandler implements ITickHandler {

    private final int TICKS = 20;
    private final int CHECK_INTERVAL = 10; // in seconds
    private final int DAY_LENGTH = 24000; // in ticks
    private final String LABEL = "SeasonsEventHandler";

    private static EntityPlayer player;

    private int m_tickCounter = 0;
    private boolean m_allowCheck = false;
    private boolean m_allowCheckWorld = false;
    private boolean m_isRemote = false;

    SeasonsManager seasonsManager = new SeasonsManager();


    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // Functions
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // Main subscribe work function
    @ForgeSubscribe
    public void onLivingUpdateEvent(LivingUpdateEvent event) {
        if (!m_allowCheck)
            return;

        // if an event isn't for the player - exit
        if (!(event.entity instanceof EntityPlayer))
            return;

        player = (EntityPlayer) event.entity;
        this.m_isRemote = player.worldObj.isRemote;

        String output = "sec " + (m_tickCounter / TICKS) + ":";
        printChat(output);

        // check parameters
        checkTime();
        checkSeason();
        checkTemperature();

        m_allowCheck = false;
    }


    // Test
    @ForgeSubscribe
    public void onWorldEvent(WorldEvent event) {
        if (!m_allowCheckWorld)
            return;

        System.out.println("# world event");

//        if (player == null) return;
        World world = event.world;

        m_allowCheckWorld = false;
    }


    // Test - check season
    public void checkSeason() {/*
        if (m_allowChangeToSummer) {
            seasonsManager.setSeason(seasonsManager.SUMMER, player);
        }
        else {
            seasonsManager.setSeason(seasonsManager.WINTER, player);
        }*/
    }


    // Test - check temperature
    public void checkTemperature() {
        String output = "Check temp... ";

        // current coordinates of player
        final int posX = MathHelper.floor_double(player.posX);
        final int posZ = MathHelper.floor_double(player.posZ);

        BiomeGenBase currentBiome = player.worldObj.getBiomeGenForCoords(posX, posZ);

        output += currentBiome.temperature;

        // output += "\ntemp int: " + currentBiome.getIntTemperature();
        // output += "\ntemp fl : " + currentBiome.getFloatTemperature();

//        System.out.println(output);
        printChat(output);
    }


    void checkTime() {
        if (player == null) return;

        // Get world
        World world = player.worldObj;

        // Get number of day
        int dayNumber = (int) (world.getWorldTime() / DAY_LENGTH);

        // Current config: 1 day = 1 season
        int season = (dayNumber % SeasonsManager.SEASONS);
        seasonsManager.setSeason(season, player);

        printChat("# WorldTime " + world.getWorldTime() + "; day " + dayNumber + "; season " + season);
    }



    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // ITickHandler
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
//        m_tickCounter++;

        // every second
        if (m_tickCounter % (TICKS) == 0) {
//            m_allowCheckWorld = true;
        }

        // every 10 seconds
        if (m_tickCounter % (CHECK_INTERVAL * TICKS) == 0) {
            m_allowCheck = true;
            m_allowCheckWorld = true;
        }

        m_tickCounter++;
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        // every second
//        if (m_tickCounter % (TICKS) != 0) return;

        seasonsManager.meltingSnow(player);
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // Additional functions
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // Output to chat
    static void printChat(String text) {
        player.sendChatToPlayer(new ChatMessageComponent().addText(text));
    }
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
}
