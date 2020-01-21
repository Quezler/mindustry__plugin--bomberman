package bomberman;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

public enum Pallete{
    cavern(Blocks.darksand, Blocks.duneRocks, Blocks.scrapWallHuge, Blocks.liquidVoid),
    spored(Blocks.shale, Blocks.sporerocks, Blocks.oilExtractor, Blocks.sporePine),
    desert(Blocks.sand, Blocks.saltRocks, Blocks.largeSolarPanel, Blocks.sandBoulder),
    forged(Blocks.hotrock, Blocks.cliffs, Blocks.multiPress, Blocks.rock),
    chanel(Blocks.water, Blocks.darkMetal, Blocks.liquidTank, Blocks.oreTitanium);

    public final Floor floor;
    public final StaticWall wall;
    public final Block blockade;
    public final Block fallback;

    Pallete(Block floor, Block wall, Block blockade, Block fallback){
        this.floor = (Floor)floor;
        this.wall = (StaticWall)wall;
        this.blockade = blockade;
        this.fallback = fallback;
    }
}
