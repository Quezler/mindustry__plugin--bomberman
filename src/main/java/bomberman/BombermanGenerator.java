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
//debugging
import static arc.util.Log.info;

public class BombermanGenerator extends Generator{
    public final static Team blockteam = Team.blue;
    public final static Block staticwall = Blocks.duneRocks;

    public final static int worldborder = 3;
    public final static int grid = 11;
    public final static int size = (grid * 3) + (worldborder * 2);

    public Array<Tile> breakable = new Array<>();
    public ArrayMap<Tile, Powerup> pWalls = new ArrayMap<>();
    //2D matrix
    public final int[][] spawns = {{34, 34}, {4, 4}, {34, 4}, {4, 34}};

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
        breakable.clear();
        //fix with arc type
        int counter = 0;
        for(int x = worldborder; x < width - worldborder; x++){
            for(int y = worldborder; y < height - worldborder; y++){
                //remove corners
                //ugly notation
                if (
                    (x == worldborder + 1 && y == worldborder + 1) ||
                    (x == worldborder + 1 && y == height - worldborder - 2) ||
                    (x == width - worldborder - 2 && y == worldborder + 1) ||
                    (x == width - worldborder - 2 && y == height - worldborder - 2)){
                    //TODO: fancy floor
                    //TODO: fill spawn array/matrix
                    continue;
                }

                //give the players some space
                if (
                    (x == worldborder + 4 && y == worldborder + 1) ||
                    (x == worldborder + 1 && y == worldborder + 4) ||
                    (x == worldborder + 4 && y == height - worldborder - 2) ||
                    (x == worldborder + 1 && y == height - worldborder - 5) ||
                    (x == width - worldborder - 2 && y == worldborder + 4) ||
                    (x == width - worldborder - 5 && y == worldborder + 1) ||
                    (x == width - worldborder - 2 && y == height - worldborder - 5) ||
                    (x == width - worldborder - 5 && y == height - worldborder - 2)
                ) {
                    continue;
                }

                if((x % 3) != 1 || (y % 3) != 1) continue;
                if(tiles[x][y].block() != Blocks.air) continue;
                breakable.add(tiles[x][y]);

                //team choice is important make sure no one uses this team !
                tiles[x][y].set(Blocks.scrapWallHuge, blockteam);
            }
        }

        // seed powerups
        for(Powerup powerup : Powerup.values()){
            Tile tmp = breakable.random();
            tmp.remove();
            //team doesn't matter now
            tmp.set(powerup.block, Team.derelict);
            pWalls.put(tmp, powerup);
        }

        world.setMap(new Map(StringMap.of("name", "Bomberman")));
    }

    enum Pallete{
        sandy(Blocks.darksand, staticwall);

        public final Floor floor;
        public final StaticWall wall;

        Pallete(Block floor, Block wall){
            this.floor = (Floor)floor;
            this.wall = (StaticWall)wall;
        }
    }
}
