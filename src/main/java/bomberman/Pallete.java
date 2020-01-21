package bomberman;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

public enum Pallete{
    cavern(Blocks.darksand,   Blocks.duneRocks,  Blocks.scrapWallHuge),
    spored(Blocks.shale,      Blocks.sporerocks, Blocks.oilExtractor),
    desert(Blocks.sand,       Blocks.saltRocks,  Blocks.largeSolarPanel),
    forged(Blocks.hotrock,    Blocks.cliffs,     Blocks.multiPress),
    chanel(Blocks.water,      Blocks.darkMetal,  Blocks.liquidTank),
    fields(Blocks.grass,      Blocks.shrubs,     Blocks.launchPad),
    snowey(Blocks.snow,       Blocks.snowrocks,  Blocks.thermalPump),
    powerd(Blocks.metalFloor5, Blocks.sandRocks,   Blocks.batteryLarge);

    public final Floor floor;
    public final StaticWall wall;
    public final Block blockade;
    public final Block fallback = Blocks.liquidVoid;

    Pallete(Block floor, Block wall, Block blockade){
        this.floor = (Floor)floor;
        this.wall = (StaticWall)wall;
        this.blockade = blockade;
    }
}
