package bomberman;

import arc.func.*;
import arc.math.*;
import mindustry.world.*;
import mindustry.content.*;

import static bomberman.BombermanGenerator.pallete;

public class Slate{
    public short x;
    public short y;
    public State state = State.undefined;

    public Slate(int x, int y) {
        this.x = (short)x;
        this.y = (short)y;
    }

    public int worldx(){
        return x * 3 + 1;
    }

    public int worldy(){
        return y * 3 + 1;
    }

    public static Slate tile(Slate[][] slates, Tile tile){
        return slates[Mathf.floor(tile.x / 3)][Mathf.floor(tile.y / 3)];
    }

    public Tile center(Tile[][] tiles){
        return tiles[worldx()][worldy()];
    }

    public void compass(Tile[][] tiles, Cons<Tile> cons){
        int offsetx = -(3 - 1) / 2;
        int offsety = -(3 - 1) / 2;
        for(int dx = 0; dx < 3; dx++){
            for(int dy = 0; dy < 3; dy++){
                cons.get(tiles[worldx() + dx + offsetx][worldy() + dy + offsety]);
            }
        }
    }

    public void draw(Tile[][] tiles){
        if(state.size == 1) center(tiles).setBlock(state.block);
        if(state.size == 3) compass(tiles, tile -> tile.setBlock(state.block));
    }

    enum State{
        // default
        undefined(pallete.fallback, 1),

        // board
        wall(pallete.wall, 3),
        scrap(pallete.blockade, 1),
        empty(Blocks.air, 1),

        // powerups
        copper    (Blocks.copperWall, 1),
        titanium  (Blocks.titaniumWall, 1),
        plastanium(Blocks.plastaniumWall, 1),
        surge     (Blocks.surgeWall, 1);


        public Block block;
        private int size;

        State(Block block, int size){
            this.block = block;
            this.size = size;
        }
    }
}
