package bomberman;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import bomberman.Slate.*;
import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

import static bomberman.Bomberman.*;
import static mindustry.Vars.*;

public class Cooties{

    private static final ObjectMap<Block, Cons<Player>> map = new ObjectMap<Block, Cons<Player>>(){{

        put(State.copper.block.get()    , player -> powerup(Powerup.copper    , player));
        put(State.titanium.block.get()  , player -> powerup(Powerup.titanium  , player));
        put(State.plastanium.block.get(), player -> powerup(Powerup.plastanium, player));
        put(State.surge.block.get()     , player -> powerup(Powerup.surge     , player));

        put(State.pyroland.block.get()  , player -> pallete());
    }};

    public static void handle(Player player, Slate slate){

        if(map.get(slate.center().block()) == null) return;
        map.get(slate.center().block()).get(player);

        Call.onConstructFinish(slate.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
        slate.state = Slate.State.empty;
    }

    private static void powerup(Powerup powerup, Player player){
        player.mech = powerup.mech;
        player.heal();
    }

    private static void pallete(){
        pallete = Structs.random(Pallete.values());

        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                world.getTiles()[x][y].setFloor(pallete.floor);
            }
        }

        slates(slate -> {
            if(slate.center().block() != Blocks.thoriumReactor) slate.place();
        });

        playerGroup.all().each(syncer -> netServer.clientCommands.handleMessage("/sync", syncer));
    }
}
