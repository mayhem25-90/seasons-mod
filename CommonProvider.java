package myseasons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/**
 *
 * The class provides functions common for both server and client sides
 *
 */

public class CommonProvider {

    static final int CHUNK_SIZE = 16;
    static final int ACTIVE_RADIUS = 8;

    // Biomes
    private static final int OCEAN = 0;
    private static final int PLAINS = 1;
    private static final int HILLS = 3;
    private static final int FOREST = 4;
    private static final int TAIGA = 5;
    private static final int RIVER = 7;
    private static final int ICE_PLAINS = 12;
    private static final int BEACH = 16;
    private static final int FOREST_HILLS = 18;
    private static final int HILLS_EDGE = 20;


    // Set temperature in active area around the player
    static void setBiomesTemperature(EntityPlayer player, float temperature) {

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
            for (int z = beginAreaZ; z < endAreaZ; z += CHUNK_SIZE) {

                // Get current biome and set temperature
                BiomeGenBase currentBiome = player.worldObj.getBiomeGenForCoords(x, z);
                if ((currentBiome.biomeID == OCEAN) || (currentBiome.biomeID == PLAINS)
                        || (currentBiome.biomeID == HILLS) || (currentBiome.biomeID == FOREST)
                        || (currentBiome.biomeID == RIVER) || (currentBiome.biomeID == BEACH)
                        || (currentBiome.biomeID == FOREST_HILLS) || (currentBiome.biomeID == HILLS_EDGE)) {
                    currentBiome.setTemperatureRainfall(temperature, currentBiome.rainfall);
                }

            }
        }

        System.out.println("# check t in player pos (" + posX + ", " + posZ + "): "
                + player.worldObj.getBiomeGenForCoords(posX, posZ).temperature);

//        player.sendChatToPlayer(new ChatMessageComponent().addText(
//                "# check t in player pos (" + posX + ", " + posZ + "): "
//                + player.worldObj.getBiomeGenForCoords(posX, posZ).temperature));
    }
}
