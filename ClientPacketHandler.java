package myseasons;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class ClientPacketHandler implements IPacketHandler{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fakePlayer) {

        // Handles incoming data
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        EntityPlayer player = (EntityPlayer) fakePlayer;

        // Channel "channel"
        if (packet.channel.equals("channel")) {
            try {
                float temperature = data.readFloat();
                CommonProvider.setBiomesTemperature(player, temperature);
            }
            catch (IOException ex) {
                System.out.println("### error " + ex);
            }
        }
    }
}
