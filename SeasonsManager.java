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
        sendTemperatureToClient();
    }


    float getTemperatureBySeason() {
        switch (currentSeason) {
            case WINTER: return 0.2F;
            case SPRING: return 0.8F;
            case SUMMER: return 0.5F;
            case AUTUMN: return 0.4F;
            default: return 0.5F;
        }
    }


    void sendTemperatureToClient() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteStream);

        try {
            outputStream.writeFloat(getTemperatureBySeason());
            PacketDispatcher.sendPacketToAllPlayers(PacketDispatcher.getPacket("channel", (byte[]) byteStream.toByteArray()));
            System.out.println("# send temp " + getTemperatureBySeason() + " to players");
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
