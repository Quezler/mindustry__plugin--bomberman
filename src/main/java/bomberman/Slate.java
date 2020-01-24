package bomberman;

import arc.*;
import arc.func.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.world.*;

import static bomberman.Bomberman.*;

import static mindustry.Vars.world;

public class Slate{
    short x;
    short y;
    State state = State.undefined;
    //import fix
    //private static Pallete pallete = Pallete.sandy;

    Slate(int x, int y) {
        this.x = (short)x;
        this.y = (short)y;
    }

    // setter
    public void setSlate(int x, int y){
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
        if(state.single) set(center(), state.block.get());
        if(!state.single) compass(tile -> set(tile, state.block.get()));
    }

    protected void set(Tile tile, Block block){
        Call.onConstructFinish(tile, block, -1, (byte)0, cake, true);
    }

    public Slate adjecent(Direction direction){
        return slates[x + direction.x][y + direction.y];
    }

    public void destroy(){
        Core.app.post(() -> {
            if(center().entity != null) center().entity.onDeath();
            if(state != State.wall) Call.onDeconstructFinish(center(), pallete.blockade, -1);
        });
    }

    enum State{
        // specials
        undefined(pallete.fallback, true),
        pyroland(Blocks.illuminator, true),

        // board
        wall (() -> pallete.wall, false),
        scrap(() -> pallete.blockade, true),
        bomb(Blocks.thoriumReactor, true),
        empty(Blocks.air, true),

        // powerups
        copper    (Blocks.copperWall, true),
        titanium  (Blocks.titaniumWall, true),
        plastanium(Blocks.plastaniumWall, true),
        surge     (Blocks.surgeWall, true);

        public Prov<Block> block;
        public boolean single;

        State(Block block, boolean single){
            this.block = () -> block;
            this.single = single;
        }

        State(Prov<Block> block, boolean single){
            this.block = block;
            this.single = single;
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

        public boolean air(){
            return (this == undefined || this == empty);
        }
    }
}
