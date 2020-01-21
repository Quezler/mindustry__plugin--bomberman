package bomberman;

import arc.func.*;
import arc.math.*;
import mindustry.game.Team;
import mindustry.world.*;
import mindustry.entities.type.*;

import static mindustry.Vars.world;

// constants and some utility functions
public class Bomberman{
    public final static int grid = 15; // odd
    public final static int size = (grid * 3);
    // team of the scrap blocks
    public final static Team blockteam = Team.sharded; //Team.blue; //TODO change
    //fix build wall error
    public final static Team aliveTeam = Team.sharded;
    //move player to this team if death
    public final static Team deathTeam = Team.green;
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
