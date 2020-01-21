package bomberman;

import arc.func.*;
import mindustry.world.*;
import mindustry.content.*;

import static bomberman.BombermanGenerator.pallete;
import static mindustry.Vars.world;

public class Slate{
    short x;
    short y;
    State state = State.undefined;

    Slate(int x, int y) {
        this.x = (short)x;
        this.y = (short)y;
    }

    // center x
    public int worldx(){
        return x * 3 + 1;
    }

    // center y
    public int worldy(){
        return y * 3 + 1;
    }

    // center tile
    public Tile center(){
        return world.getTiles()[worldx()][worldy()];
    }

    // all 9 tiles in this slate
    public void compass(Cons<Tile> cons){
        int offsetx = -(3 - 1) / 2;
        int offsety = -(3 - 1) / 2;
        for(int dx = 0; dx < 3; dx++){
            for(int dy = 0; dy < 3; dy++){
                cons.get(world.getTiles()[worldx() + dx + offsetx][worldy() + dy + offsety]);
            }
        }
    }

    // places either 1 big or 9 small ones
    public void place(){
        if(state.center) center().setBlock(state.block);
        if(!state.center) compass(tile -> tile.setBlock(state.block));
    }

    enum State{
        // default
        undefined(pallete.fallback, true),

        // board
        wall (pallete.wall, false),
        scrap(pallete.blockade, true),
        empty(Blocks.air, true),

        // powerups
        copper    (Blocks.copperWall, true),
        titanium  (Blocks.titaniumWall, true),
        plastanium(Blocks.plastaniumWall, true),
        surge     (Blocks.surgeWall, true);

        public Block block;
        public boolean center;

        State(Block block, boolean center){
            this.block = block;
            this.center = center;
        }

        public boolean powerup(){
            if(this == copper)     return true;
            if(this == titanium)   return true;
            if(this == plastanium) return true;
            if(this == surge)      return true;

            return false;
        }

        public boolean flyable(){
            if(this == wall)  return false;
            if(this == scrap) return false;

            return true;
        }
    }
}
