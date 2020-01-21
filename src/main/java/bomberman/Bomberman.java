package bomberman;

import arc.func.*;
import arc.math.*;
import mindustry.entities.type.*;
import mindustry.world.*;

import static mindustry.Vars.world;

public class Bomberman{
    public final static int grid = 15; // odd
    public final static int size = (grid * 3);
    public final static String mapname = "[royal]Bomberman [white]\uF831";
    public static Slate[][] slates = new Slate[grid][grid];

    // resolve tile from player
    public static Tile tile(Player player){
        return world.tile(world.toTile(player.x), world.toTile(player.y));
    }

    // resolve slate from tile
    public static Slate slate(Tile tile){
        return slates[Mathf.floor(tile.x / 3)][Mathf.floor(tile.y / 3)];
    }

    // consume all slates
    public static void slates(Cons<Slate> cons){
        for(int x = 0; x < slates.length; x++){
            for(int y = 0; y < slates[0].length; y++){
                cons.get(slates[x][y]);
            }
        }
    }
}
