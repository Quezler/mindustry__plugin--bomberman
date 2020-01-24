package bomberman;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import bomberman.Slate.*;
import mindustry.maps.*;
import mindustry.maps.generators.*;
import mindustry.world.*;

import static bomberman.Bomberman.*;
import static mindustry.Vars.*;

// class that generates the map
public class BombermanGenerator extends Generator{

    public static final Pallete pallete = Structs.random(Pallete.values());

    BombermanGenerator(){
        super(grid * 3, grid * 3);
    }

    @Override
    public void generate(Tile[][] tiles){

        // init tiles (1x1)
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                tiles[x][y] = new Tile(x, y);
                tiles[x][y].setFloor(pallete.floor);
            }
        }

        // init slates (3x3)
        for(int x = 0; x < slates.length; x++){
            for(int y = 0; y < slates[0].length; y++){
                slates[x][y] = new Slate(x, y);
            }
        }

        seed();

        // place slates (gen)
        slates(s -> {
            if(s.state == State.wall) s.place();
        });

        world.setMap(new Map(StringMap.of("name", mapname)));
    }

    public void seed(){
        // reset state (default)
        slates(slate -> slate.state = State.undefined);

        // set walls (border)
        slates(slate -> {
            if(slate.x == 0) slate.state = State.wall; // left
            if(slate.y == 0) slate.state = State.wall; // bottom
            if(slate.x == slates   .length - 1) slate.state = State.wall; // right
            if(slate.y == slates[0].length - 1) slate.state = State.wall; // top
        });

        // set walls (grid)
        slates(slate -> {
            if(!slate.state.air()) return;
            if((slate.x % 2) == 0) slate.state = State.wall;
            if((slate.y % 2) == 0) slate.state = State.wall;
            if((slate.x % 2) == 0 ^ (slate.y % 2) == 0) slate.state = State.undefined;
        });

        // fill map (scrap)
        slates(slate -> {
            if(!slate.state.air()) return;
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

    }

    public Array<Spawn> getSpawns(){
        int [][] ret = new int[4][2];
        Slate tmp = new Slate(0,0);
        int counter = 0;
        for (int x = 1; x < slates[0].length; x += slates[0].length - 3){
            for (int y = 1; y < slates.length; y += slates.length - 3){
                tmp.setSlate(x, y);
                ret[counter][0] = tmp.worldx();
                ret[counter][1] = tmp.worldy();
                counter++;
            }
        }

        Array<Spawn> spawns = new Array<>();
        for(int[] coords : ret){
            spawns.add(new Spawn(coords[0] * tilesize, coords[1] * tilesize));
        }

//        Log.info(spawns);

        return spawns;
    }
}
