package bomberman;

import arc.func.*;
import arc.math.*;

import arc.struct.*;

import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.world.*;

import static mindustry.Vars.*;

// constants and some utility functions
public class Bomberman{
    public final static int grid = 15; // odd
    public final static String mapname = "[royal]Bomberman [white]\uF831";
    public static Slate[][] slates = new Slate[grid][grid];

    public final static Team[] teams = new Team[]{Team.crux, Team.green, Team.purple, Team.blue};
    public final static Team dead = Team.derelict;
    public final static Team cake = Team.sharded;

    public static ObjectIntMap<Team> bombs = new ObjectIntMap<>();
    public static ObjectMap<Tile, Player> nukes = new ObjectMap<>();

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


    public static Phase phase = Phase.playing;

    enum Phase{
        playing,
        resetting,
    };
}
