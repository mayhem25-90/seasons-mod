package myseasons;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class SeasonsEventHandler implements ITickHandler {

    private final int TICKS = 20;
    private final int CHECK_INTERVAL = 2; // in seconds

    private int mTickCounter = 0;

    private SeasonsManager seasonsManager = new SeasonsManager();


    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // Functions
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    // Main subscribe work function
    void onTick() {

        // Every tick
//        seasonsManager.meltingSnowBase();

        // Every second
        if (mTickCounter % (TICKS) == 0) {
            seasonsManager.checkSeason();
        }

        // Every <CHECK_INTERVAL> seconds
//        if (mTickCounter % (CHECK_INTERVAL * TICKS) == 0);
    }


    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // ITickHandler
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.SERVER))) {
            onTick();
            mTickCounter++;
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return "";
    }
    // - --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
}
