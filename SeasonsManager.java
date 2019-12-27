package myseasons;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

public class SeasonsManager {

    // World
    private static final int BIOMES = 23;
    private static final int DAY_LENGTH = 24000; // in ticks
    private static final int CHUNK_SIZE = CommonProvider.CHUNK_SIZE;
    private static final int ACTIVE_RADIUS = CommonProvider.ACTIVE_RADIUS;

    // Block IDs
    private static final int AIR = 0;
    private static final int WATER = 8;
    private static final int SNOW_LAYER = 78;
    private static final int ICE = 79;

    // Seasons
    private static final int WINTER = 0;
    private static final int SPRING = 1;
    private static final int SUMMER = 2;
    private static final int AUTUMN = 3;

    private static final int EARLY = 0;
    private static final int MID = 1;
    private static final int LATE = 2;

    private static final int SEASONS = 4;
    private static final int SUBSEASONS = 3;
    private static final String[] seasonSuffix = {"winter", "spring", "summer", "autumn"};
    private static final String[] subseasonSuffix = {"early", "middle", "late"};

    // Temperature at seasons
    private static final float[] TEMPERATURE =
            { 0.20F, 0.20F, 0.20F, 0.50F, 0.60F, 0.70F, 0.80F, 0.90F, 0.80F, 0.40F, 0.30F, 0.25F };

    int currentSeason = -1;
    int currentSubseason = -1;

    WorldServer world;


    public void checkSeason() {

        // Get world
        world = DimensionManager.getWorld(0);

        if (world == null) return;

        // Get number of day
        final int dayNumber = (int) (world.getWorldTime() / DAY_LENGTH);

        // Current config: 3 days = 1 season (1 day = 1 subseason)
        int month = dayNumber % (SEASONS * SUBSEASONS);
        int season = month / SUBSEASONS;
        int subseason = month % SUBSEASONS;

        System.out.println();
        System.out.println("# WorldTime " + world.getWorldTime() + "; day " + dayNumber);
        System.out.println("# Season " + season + " (" + seasonSuffix[season] + ")"
                + " subseason " + subseason + " (" + subseasonSuffix[subseason] + ")");


        // Set season if changed
        if ((currentSeason != season) || (currentSubseason != subseason)) {

            // Info message about the season to all players
            for (EntityPlayer player : (ArrayList<EntityPlayer>) world.playerEntities) {
                player.sendChatToPlayer(new ChatMessageComponent().addText(
                        ">>> Now is " + subseasonSuffix[subseason] + " " + seasonSuffix[season] + " <<<"));
            }

            // Set season and grass/foliage color...
            currentSeason = season;
            currentSubseason = subseason;
            setGrassFoliageColor();
        }

        // Always set temperature, because active area of player can offset by player's moving
        for (EntityPlayer player : (ArrayList<EntityPlayer>) world.playerEntities) {
            CommonProvider.setBiomesTemperature(player, getTemperatureBySeason());
        }
        sendTemperatureToClient(getTemperatureBySeason());
    }


    float getTemperatureBySeason() {

        // Get current month
        int month = (currentSeason * SUBSEASONS) + currentSubseason;

        if ((month >= 0) && (month < SEASONS * SUBSEASONS)) {
            return TEMPERATURE[month];
        }
        else return 0.5F;
    }


    void sendTemperatureToClient(float temperature) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteStream);

        try {
            outputStream.writeFloat(temperature);
            PacketDispatcher.sendPacketToAllPlayers(PacketDispatcher.getPacket("channel", (byte[]) byteStream.toByteArray()));
            System.out.println("# send temp " + temperature + " to players");
        }
        catch (IOException ex) {
            System.out.println("### error " + ex);
            ex.printStackTrace();
        }
    }


    void setGrassFoliageColor() {

        if (FMLCommonHandler.instance().getSide() != Side.CLIENT) return;

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


    // randomly melt ice and snow when it isn't winter
    void meltingSnow() {

        if ((world == null) || (currentSeason == WINTER)) return;

        for (EntityPlayer player : (ArrayList<EntityPlayer>) world.playerEntities) {

            if (player == null) return;

            // Frequency of random tried to melt snow
            int trying = 1;
            if (currentSeason == SPRING) {
                if (currentSubseason == EARLY) trying = 8;
                else if (currentSubseason == MID) trying = 4;
                else if (currentSubseason == LATE) trying = 2;
            }

            // Try to random
            if (world.rand.nextInt(trying) != 0) return;

            // Current coordinates of player
            final int posX = MathHelper.floor_double(player.posX);
            final int posZ = MathHelper.floor_double(player.posZ);

            // Get active area
            Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);

            // Search chunks for melting snow in active player radius
            final int beginAreaX = (chunk.xPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
            final int beginAreaZ = (chunk.zPosition - ACTIVE_RADIUS) * CHUNK_SIZE;
            final int endAreaX = (chunk.xPosition + ACTIVE_RADIUS) * CHUNK_SIZE;
            final int endAreaZ = (chunk.zPosition + ACTIVE_RADIUS) * CHUNK_SIZE;

            // Iteration on X, Z with step a chunk
            for (int x = beginAreaX; x < endAreaX; x += CHUNK_SIZE) {
                for (int z = beginAreaZ; z < endAreaZ; z += CHUNK_SIZE) {

                    // get random XZ-offset for melting in chunk
                    int updateLCG = (new Random()).nextInt() * 3 + 1013904223;
                    int randOffset = updateLCG >> 2;

                    x += (randOffset & 15);
                    z += (randOffset >> 8 & 15);

                    // is the temperature good for melting snow?...
                    if (isFreezeAt(x, z)) return;


                    // going upside down each column
                    for (int y = world.getPrecipitationHeight(x, z); y > 0; --y) {

                        int block = world.getBlockId(x, y, z);

                        if (block == AIR) continue;

                        if (block == SNOW_LAYER) world.setBlockToAir(x, y, z);

                        if (block == ICE) world.setBlock(x, y, z, WATER);

                        break;
                    }
                }
            }
        }
    }


    // Check is freeze at current point
    boolean isFreezeAt(final int x, final int z) {

        String output = "melting snow?... ";

        BiomeGenBase currentBiome = world.getBiomeGenForCoords(x, z);
        output += "temp: " + currentBiome.getFloatTemperature();
        if (currentBiome.getFloatTemperature() < 0.15F) {
            System.out.println(output + " => return");
            return true;
        }

        System.out.println(output + " the temperature is good, let's melt snow");

        return false;
    }
}
