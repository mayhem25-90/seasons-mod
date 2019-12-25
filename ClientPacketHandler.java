package myseasons;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class ClientPacketHandler implements IPacketHandler{

    private static final int CHUNK_SIZE = 16;
    private static final int ACTIVE_RADIUS = 8;

    // Biomes
    private static final int TAIGA = 5;
    private static final int ICE_PLAINS = 12;

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fakePlayer) {

        // Handles incoming data
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));

        EntityPlayer player = (EntityPlayer) fakePlayer;


        if (packet.channel.equals("channel")) {
            try {
                float temparature = data.readFloat();

                // TODO: need to correct duplicate of code...

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
                        if ((currentBiome.biomeID != TAIGA) && (currentBiome.biomeID != ICE_PLAINS)) {
                            currentBiome.setTemperatureRainfall(temparature, currentBiome.rainfall);
                        }

                    }
                }

//                player.sendChatToPlayer(new ChatMessageComponent().addText(
//                        "# check t in player pos (" + posX + ", " + posZ + "): "
//                        + player.worldObj.getBiomeGenForCoords(posX, posZ).temperature));
            }
            catch (IOException ex) {
                System.out.println("### error " + ex);
                ex.printStackTrace();
            }
        }
    }
}
