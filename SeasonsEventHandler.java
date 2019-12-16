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

public class SeasonsEventHandler implements ITickHandler {

    private final int TICKS = 20;
    private final int CHECK_INTERVAL = 10; // in seconds
    private final int DAY_LENGHT = 24000; // in ticks
    private final String LABEL = "SeasonsEventHandler";

    private final int AIR = 0;
    private final int WATER = 8;
    private final int SNOW_LAYER = 78;
    private final int ICE = 79;

    private static EntityPlayer player;

    private int m_tickCounter = 0;
    private boolean m_allowCheck = false;
    private boolean m_allowCheckBiome = false;
    private boolean m_allowChangeToSummer = false;
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
    public void onBiomeEvent(BiomeEvent event) {
        if (!m_allowCheckBiome)
            return;

//        System.out.println("# biome event");

//        if (player == null) return;

        m_allowCheckBiome = false;
    }


    // Test - check season
    public void checkSeason() {
        if (m_allowChangeToSummer) {
            seasonsManager.setSeason(seasonsManager.SUMMER, player);
        }
        else {
            seasonsManager.setSeason(seasonsManager.WINTER, player);
        }
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

        printChat("# WorldTime " + player.worldObj.getWorldTime());
    }


    // random melt snow every tick if it isn't winter
    void meltingSnow() {
        if (player == null) return;

        String output = "melting snow?... ";

        // get world
        World world = player.worldObj;

        // frequency of random tried to melt snow
        int trying = 1;
        if(world.rand.nextInt(trying) != 0) return;

        // current coordinates of player
        final int posX = MathHelper.floor_double(player.posX);
        final int posZ = MathHelper.floor_double(player.posZ);

        BiomeGenBase currentBiome = world.getBiomeGenForCoords(posX, posZ);
        output += "temp: " + currentBiome.getFloatTemperature();
        if (currentBiome.getFloatTemperature() < 0.15F) {
            System.out.println(output + " => return");
            return;
        }

        // the temperature is good, let's melt snow
        System.out.println(output + " the temperature is good, let's melt snow");
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);

//        Iterator<Chunk> iter = world.getPersistentChunks();
//        WorldServer w;
//        w.theChunkProviderServer;
//        world.chunk

        // test - manual search
//        int xChunkStart = chunk.xPosition * CHUNK_SIZE;
//        int zChunkStart = chunk.zPosition * CHUNK_SIZE;
//        int xChunkEnd = chunk.xPosition * CHUNK_SIZE + CHUNK_SIZE;
//        int zChunkEnd = chunk.zPosition * CHUNK_SIZE + CHUNK_SIZE;

//        System.out.println("# chunk x start: " + xChunkStart + ", chunk z start: " + zChunkStart);
//        System.out.println("# chunk x end  : " + xChunkEnd + ", chunk z end  : " + zChunkEnd);

        // search chunks for melting snow in active player radius
        final int beginAreaX = (chunk.xPosition - SeasonsManager.ACTIVE_RADIUS) * SeasonsManager.CHUNK_SIZE;
        final int beginAreaZ = (chunk.zPosition - SeasonsManager.ACTIVE_RADIUS) * SeasonsManager.CHUNK_SIZE;
        final int endAreaX = (chunk.xPosition + SeasonsManager.ACTIVE_RADIUS) * SeasonsManager.CHUNK_SIZE;
        final int endAreaZ = (chunk.zPosition + SeasonsManager.ACTIVE_RADIUS) * SeasonsManager.CHUNK_SIZE;


        for (int x = beginAreaX; x < endAreaX; x += SeasonsManager.CHUNK_SIZE) {
            for (int z = beginAreaZ; z < endAreaZ; z += SeasonsManager.CHUNK_SIZE) {

                // get random XZ-offset for melting in chunk
                int updateLCG = (new Random()).nextInt() * 3 + 1013904223;
                int randOffset = updateLCG >> 2;

                x += (randOffset & 15);
                z += (randOffset >> 8 & 15);
        
//                System.out.println("magic x: " + (x + (randOffset & 15))
//                        + ", z: " + (z + (randOffset >> 8 & 15)));
//                System.out.println("magic x: " + x + ", z: " + z);

                int precH = world.getPrecipitationHeight(x, z);
//                int precH = world.getPrecipitationHeight(x + (randOffset & 15), z + (randOffset >> 8 & 15));
//                System.out.println("precipitation height at: " + x + ", " + z + ": " + precH);

                // going upside down each column
                for (int y = precH; y > 0; --y) {
                    int blockID = world.getBlockId(x, y, z);
                    System.out.println("Block ID at (" + x + ", " + z + ") " + y + ": " + blockID);

                    if (blockID == AIR) 
                        continue;

                    if (blockID == SNOW_LAYER) {
                        world.setBlockToAir(x, y, z);
                    }

                    if (blockID == ICE)
                    {
                        world.setBlock(x, y, z, WATER);
                    }

                    break;
                }
            }
        }
    }
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // ITickHandler
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        m_tickCounter++;

        // every second
        if (m_tickCounter % (TICKS) == 0) {
            m_allowCheckBiome = true;
        }

        if (m_tickCounter % (CHECK_INTERVAL * TICKS) == 0) {
            m_allowCheck = true;
        }

        // change season to SUMMER every 5 intervals
        if (m_tickCounter % (5 * CHECK_INTERVAL * TICKS) == 0) {
            m_allowChangeToSummer = true;
        }

//        m_tickCounter++;
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        // every second
//        if (m_tickCounter % (TICKS) != 0) return;

        meltingSnow();
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
