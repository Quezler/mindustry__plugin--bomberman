package bomberman;

import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.maps.*;
import bomberman.Slate.*;
import mindustry.world.*;
import mindustry.maps.generators.*;

import static bomberman.Bomberman.*;
import static mindustry.Vars.world;

// class that generates the map
public class BombermanGenerator extends Generator{
    public final int[][] spawns = {{4, 4}};

    public static final Pallete pallete = Pallete.sandy;

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

        // spawn points (corners) - remove some squares
        slates(slate -> {
            if(slate.state != State.scrap) return;

            if(slate.x < 2) slate.state = State.empty;
            if(slate.y < 2) slate.state = State.empty;
            if(slate.x > slates[0].length - 3) slate.state = State.empty;
            if(slate.y > slates   .length - 3) slate.state = State.empty;

            //fill up the blank spots
            if(slate.x < 3 ^ slate.y < 3 ^ slate.x > slates[0].length - 4 ^ slate.y > slates.length - 4) slate.state = State.scrap;

        });

        // seed powerups (random)
        slates(slate -> {
            if(slate.state != State.scrap) return;
            if(!Mathf.chance(0.025)) return;

            slate.state = Structs.random(Powerup.values()).slate;
        });

        // place slates (gen)
        slates(Slate::place);

        world.setMap(new Map(StringMap.of("name", mapname)));
    }
}
