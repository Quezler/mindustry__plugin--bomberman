package bomberman;

import arc.func.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.maps.*;
import bomberman.Slate.*;
import mindustry.world.*;
import mindustry.content.*;
import bomberman.BombermanMod.*;
import mindustry.world.blocks.*;
import mindustry.maps.generators.*;

import static mindustry.Vars.world;

public class BombermanGenerator extends Generator{
    public final static int grid = 15; // odd
    public final static int size = (grid * 3);

    public final int[][] spawns = {{4, 4}};

    public static final Pallete pallete = Pallete.sandy;

    public Slate[][] slates = new Slate[grid][grid];

    BombermanGenerator(){
        super(size, size);
    }

    @Override
    public void generate(Tile[][] tiles){

        // init tiles (1x1)
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                tiles[x][y] = new Tile(x, y);
            }
        }

        // init slates (3x3)
        for(int x = 0; x < slates.length; x++){
            for(int y = 0; y < slates[0].length; y++){
                slates[x][y] = new Slate(x, y);
            }
        }

        // set walls (border)
        slates(slate -> {
            if(slate.x == 0) slate.state = State.wall; // left
            if(slate.y == 0) slate.state = State.wall; // bottom
            if(slate.x == slates   .length - 1) slate.state = State.wall; // right
            if(slate.y == slates[0].length - 1) slate.state = State.wall; // top
        });

        // set walls (grid)
        slates(slate -> {
            if(slate.state != State.undefined) return;
            if((slate.x % 2) == 0) slate.state = State.wall;
            if((slate.y % 2) == 0) slate.state = State.wall;
            if((slate.x % 2) == 0 ^ (slate.y % 2) == 0) slate.state = State.undefined;
        });

        // fill map (scrap)
        slates(slate -> {
            if(slate.state != State.undefined) return;
            if((slate.x % 2) == 1) slate.state = State.scrap;
            if((slate.y % 2) == 1) slate.state = State.scrap;
        });

        // spawn points (corners)
        slates(slate -> {
            if(slate.state != State.scrap) return;

            // bottom left
            if(slate.x < 2) slate.state = State.empty;
            if(slate.y < 2) slate.state = State.empty;
            if(slate.x < 3 ^ slate.y < 3) slate.state = State.scrap;

            // todo: top left
            // todo: bottom right
            // todo: top right
        });

        // seed powerups (random)
        slates(slate -> {
            if(slate.state != State.scrap) return;
            if(!Mathf.chance(0.025)) return;

            slate.state = Structs.random(Powerup.values()).block;
        });

        // draw slates (write)
        slates(slate -> slate.draw(tiles));

        world.setMap(new Map(StringMap.of("name", "Bomberman")));
    }

    public void slates(Cons<Slate> cons){
        for(int x = 0; x < slates.length; x++){
            for(int y = 0; y < slates[0].length; y++){
                cons.get(slates[x][y]);
            }
        }
    }

    enum Pallete{
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
}
