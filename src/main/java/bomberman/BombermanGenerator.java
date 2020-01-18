package bomberman;

import arc.maps.*;
import arc.math.Interpolation.*;
import arc.struct.*;
import bomberman.BombermanMod.*;
import mindustry.game.*;
import mindustry.maps.*;
import mindustry.world.*;
import mindustry.content.*;
import mindustry.world.blocks.*;
import mindustry.maps.generators.*;

import static mindustry.Vars.world;

public class BombermanGenerator extends Generator{
    public final static int worldborder = 3;
    public final static int grid = 11;
    public final static int size = (grid * 3) + (worldborder * 2);

    public Array<Tile> breakable = new Array<>();

    BombermanGenerator(){
        super(size, size);
    }

    @Override
    public void generate(Tile[][] tiles){

        Pallete pallete = Pallete.sandy;

        // init stuff
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                tiles[x][y] = new Tile(x, y, pallete.floor.id, Blocks.air.id, Blocks.air.id);
            }
        }

        // world border
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(x < worldborder || y < worldborder || (width - x) <= worldborder || (height - y) <= worldborder) tiles[x][y].setBlock(pallete.wall);
            }
        }

        // unbreakables
        for(int x = worldborder; x < width - worldborder; x++){
            for(int y = worldborder; y < height - worldborder; y++){
                if(((y / 3) % 2) == 0) tiles[x][y].setBlock(pallete.wall);
                if(((x / 3) % 2) == 0) tiles[x][y].setBlock(pallete.wall);

                if(((y / 3) % 2) == 0 ^ ((x / 3) % 2) == 0) tiles[x][y].setBlock(Blocks.air);
            }
        }

        // scrap

//        for(int x = worldborder; x < width - worldborder; x++){
//            for(int y = worldborder; y < height - worldborder; y++){
//
//                if(((y / 3) % 2) == 1 && (x % 3) == 1 && ((x / 3) % 2) == 1 && (y % 3) == 1) tiles[x][y].setBlock(Blocks.scrapWall);
//            }
//        }

        // scrap
        breakable.clear();
        for(int x = worldborder; x < width - worldborder; x++){
            for(int y = worldborder; y < height - worldborder; y++){

                if((x % 3) != 1 || (y % 3) != 1) continue;
                if(tiles[x][y].block() != Blocks.air) continue;
                breakable.add(tiles[x][y]);
                tiles[x][y].set(Blocks.scrapWallHuge, Team.derelict);
            }
        }

        // seed powerups
        for(Powerup powerup : Powerup.values()){
            Tile tmp = breakable.random();
            tmp.remove();
            tmp.set(powerup.block, Team.derelict);
        }

        world.setMap(new Map(StringMap.of("name", "Bomberman")));
    }

    enum Pallete{
        sandy(Blocks.darksand, Blocks.duneRocks);

        public final Floor floor;
        public final StaticWall wall;

        Pallete(Block floor, Block wall){
            this.floor = (Floor)floor;
            this.wall = (StaticWall)wall;
        }
    }
}
