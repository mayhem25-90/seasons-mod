package myseasons;

import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class SeasonsManager {

    private static final int CHUNK_SIZE = 16;
    private static final int ACTIVE_RADIUS = 8;

    private static final int AIR = 0;
    private static final int WATER = 8;
    private static final int SNOW_LAYER = 78;
    private static final int ICE = 79;

    private static final int WINTER = 0;
    private static final int SPRING = 1;
    private static final int SUMMER = 2;
    private static final int AUTUMN = 3;

    static final int SEASONS = 4;
    static final String[] seasonSuffix = {"winter", "spring", "summer", "autumn"};

    int currentSeason = SUMMER;

    WorldServer world;
    EntityPlayer player;


    public void setWorld(WorldServer world) {
        this.world = world;
    }


    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }


    public void setSeason(final int season) {

        if (currentSeason != season) {
            SeasonsEventHandler.printChat("### Set " + seasonSuffix[season] + " ###");
            currentSeason = season;
//            setGrassFoliageColor();
        }

        if (world == null) return;

        switch (season) {
        case WINTER:
            for (EntityPlayer pl : (ArrayList<EntityPlayer>) world.playerEntities) {
                setBiomesTemperature(player, 0.0F, 0.5F);
            }
            break;

        case SPRING:
            for (EntityPlayer pl : (ArrayList<EntityPlayer>) world.playerEntities) {
                setBiomesTemperature(player, 0.35F, 0.5F);
            }
            break;

        case SUMMER:
            for (EntityPlayer pl : (ArrayList<EntityPlayer>) world.playerEntities) {
                setBiomesTemperature(player, 0.7F, 0.5F);
            }
            break;

        case AUTUMN:
            for (EntityPlayer pl : (ArrayList<EntityPlayer>) world.playerEntities) {
                setBiomesTemperature(player, 2.0F, 0.5F);
            }

        default:
            break;
        }
    }


    void setBiomesTemperature(EntityPlayer player, float temperature, float rainfall) {

        SeasonsEventHandler.printChat("set temperature " + temperature);
//        System.out.println("### set temperature " + temperature);

        // Current coordinates of player
        final int posX = MathHelper.floor_double(player.posX);
        final int posZ = MathHelper.floor_double(player.posZ);

        // Get active area
        Chunk chunk = player.worldObj.getChunkFromBlockCoords(posX, posZ);

        final int beginAreaX = (chunk.xPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int beginAreaZ = (chunk.zPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaX = (chunk.xPosition + ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaZ = (chunk.zPosition + ACTIVE_RADIUS) * CHUNK_SIZE;

        // Iteration on X, Z with step 16 for set temperature in biomes
        for (int x = beginAreaX; x < endAreaX; x += CHUNK_SIZE) {
            for (int z = beginAreaZ; x < endAreaZ; x += CHUNK_SIZE) {
                BiomeGenBase currentBiome = player.worldObj.getBiomeGenForCoords(x, z);
                currentBiome.setTemperatureRainfall(temperature, currentBiome.getFloatRainfall());
//                currentBiome.setTemperatureRainfall(temperature, rainfall);
            }
        }
    }


    void setGrassFoliageColor() {

        final ResourceLocation grassColormap = new ResourceLocation(
                MainClass.MODID + ":textures/colormap/grass_" + seasonSuffix[currentSeason] + ".png");
        final ResourceLocation foliageColormap = new ResourceLocation(
                MainClass.MODID + ":textures/colormap/foliage_" + seasonSuffix[currentSeason] + ".png");

        ResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
        try
        {
            ColorizerGrass.setGrassBiomeColorizer(TextureUtil.readImageData(resManager, grassColormap));
            ColorizerFoliage.setFoliageBiomeColorizer(TextureUtil.readImageData(resManager, foliageColormap));
        }
        catch (IOException ioexception)
        {
            System.out.println("### error setGrassBiomeColorizer " + ioexception);
            ioexception.printStackTrace();
        }
    }


    void meltingSnowBase() {
        if (world == null) return;

        for (EntityPlayer pl : (ArrayList<EntityPlayer>) world.playerEntities) {
            meltingSnow(pl);
        }
    }


    // randomly melt ice and snow when it isn't winter
    void meltingSnow(EntityPlayer player) {
        if (player == null) return;

//        if (world == null) return;

        String output = "melting snow?... ";

        // Get world
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

        // TODO: this is a bug: we need to check temperature in each XZ
        // the temperature is good, let's melt snow
        System.out.println(output + " the temperature is good, let's melt snow");
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);


        // search chunks for melting snow in active player radius
        final int beginAreaX = (chunk.xPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int beginAreaZ = (chunk.zPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaX = (chunk.xPosition + ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaZ = (chunk.zPosition + ACTIVE_RADIUS) * CHUNK_SIZE;

        for (int x = beginAreaX; x < endAreaX; x += CHUNK_SIZE) {
            for (int z = beginAreaZ; z < endAreaZ; z += CHUNK_SIZE) {

                // get random XZ-offset for melting in chunk
                int updateLCG = (new Random()).nextInt() * 3 + 1013904223;
                int randOffset = updateLCG >> 2;

                x += (randOffset & 15);
                z += (randOffset >> 8 & 15);

//                System.out.println("magic x: " + x + ", z: " + z);

                int precH = world.getPrecipitationHeight(x, z);
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
}
