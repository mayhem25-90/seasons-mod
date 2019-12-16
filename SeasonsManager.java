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

public class SeasonsManager {

    private final int CHUNK_SIZE = 16;
    private final int ACTIVE_RADIUS = 16;

    public static final int WINTER = 0;
    public static final int SUMMER = 2;


    public void setSeason(final int season, EntityPlayer player) {
        switch (season) {
        case WINTER:
            System.out.println("### Set winter ###");
            SeasonsEventHandler.printChat("### Set winter ###");
            setBiomesTemperature(player, 0.05F);
            break;

        case SUMMER:
            System.out.println("### Set summer ###");
            SeasonsEventHandler.printChat("### Set summer ###");
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

        // Get world
        World world = player.worldObj;

        final int beginX = posX - ACTIVE_RADIUS * CHUNK_SIZE;
        final int beginZ = posZ - ACTIVE_RADIUS * CHUNK_SIZE;
        final int endX = posX + ACTIVE_RADIUS * CHUNK_SIZE;
        final int endZ = posZ + ACTIVE_RADIUS * CHUNK_SIZE;


        System.out.println("# enable snow");

        // Iteration on X, Z with step 16 for enable snow in biomes
        for (int x = beginX; x < endX; x += CHUNK_SIZE) {
            for (int z = beginZ; x < endZ; x += CHUNK_SIZE) {
                BiomeGenBase currentBiome = world.getBiomeGenForCoords(x, z);
                currentBiome.setTemperatureRainfall(0.05F, currentBiome.getFloatRainfall());
            }
        }
    }
}
