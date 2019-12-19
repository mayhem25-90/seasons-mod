package myseasons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;

public class PlayerInterface {

    // Output to chat
    static void printChat(EntityPlayer player, String text) {
        player.sendChatToPlayer(new ChatMessageComponent().addText(text));
    }
}
