package bomberman;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

public enum Pallete{
    sandy(Blocks.darksand, Blocks.duneRocks, Blocks.scrapWallHuge, Blocks.liquidVoid);

    public final Floor floor;
    public final StaticWall wall;
    public final Block blockade; // 3x3
    public final Block fallback;

    Pallete(Block floor, Block wall, Block blockade, Block fallback){
        this.floor = (Floor)floor;
        this.wall = (StaticWall)wall;
        this.blockade = blockade;
        this.fallback = fallback;
    }
}
