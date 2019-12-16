package myseasons;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class SeasonsManager {

    public static final int CHUNK_SIZE = 16;
    public static final int ACTIVE_RADIUS = 8;

    public static final int WINTER = 0;
    public static final int SPRING = 1;
    public static final int SUMMER = 2;
    public static final int AUTUMN = 3;

    String[] seasonSuffix = {"winter", "spring", "summer", "autumn"};

    int currentSeason = SUMMER;


    public void setSeason(final int season, EntityPlayer player) {
        System.out.println("### Set " + seasonSuffix[season] + " ###");
        SeasonsEventHandler.printChat("### Set " + seasonSuffix[season] + " ###");

        currentSeason = season;
        setGrassFoliageColor();

        switch (season) {
        case WINTER:
            setBiomesTemperature(player, 0.05F);
            break;

        case SUMMER:
            setBiomesTemperature(player, 0.7F);
            break;

        default:
            break;
        }
    }


    public void setBiomesTemperature(EntityPlayer player, float temp) {

        // Current coordinates of player
        final int posX = MathHelper.floor_double(player.posX);
        final int posZ = MathHelper.floor_double(player.posZ);

        // Get active area
        World world = player.worldObj;
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);

        final int beginAreaX = (chunk.xPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int beginAreaZ = (chunk.zPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaX = (chunk.xPosition + ACTIVE_RADIUS) * CHUNK_SIZE;
        final int endAreaZ = (chunk.zPosition + ACTIVE_RADIUS) * CHUNK_SIZE;

        // Iteration on X, Z with step 16 for set temperature in biomes
        for (int x = beginAreaX; x < endAreaX; x += CHUNK_SIZE) {
            for (int z = beginAreaZ; x < endAreaZ; x += CHUNK_SIZE) {
                BiomeGenBase currentBiome = world.getBiomeGenForCoords(x, z);
                currentBiome.setTemperatureRainfall(temp, currentBiome.getFloatRainfall());
            }
        }
    }


    void setGrassFoliageColor() {
//        final ResourceLocation field_130078_a = new ResourceLocation("textures/colormap/grass.png");
//        final ResourceLocation field_130079_a = new ResourceLocation("textures/colormap/foliage.png");
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
            System.out.println("### error setGrassBiomeColorizer" + ioexception);
            ioexception.printStackTrace();
        }
    }
}
