package bomberman;

import arc.*;
import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.entities.effect.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.plugin.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.BuildBlock.*;

import static arc.util.Log.info;
import static bomberman.Bomberman.*;
import static mindustry.Vars.*;

public class BombermanMod extends Plugin{
    private final Rules rules = new Rules();

    private static BombermanGenerator generator;


    @Override
    public void init(){
        info("senpai bomberman <3");
        rules.tags.put("bomberman", "true");
        rules.infiniteResources = true;
        rules.canGameOver = false;
        rules.playerDamageMultiplier = 0.1f;

        //Todo: check for min 2 players and have a countdown (~ 10 seconds)
        //if game is already running -> spectator mode
        Events.on(PlayerJoin.class, event -> {
            if(!active()) return;

            event.player.kill();
            event.player.dead = true;
            event.player.setTeam(dead);
        });

        // primary event loop
        Events.on(Trigger.update, () -> {
            if(!active()) return;

            if(stage.current() == null) stage.set(Stage.waiting);

            stage.update();
        });

        // prevent placing blocks
        Events.on(BlockBuildEndEvent.class, event -> {
            if(event.player == null) return;
            if(!active()) return;

            if(event.breaking){
                if(event.tile.block() instanceof BuildBlock && (((BuildEntity)event.tile.ent()).previous == pallete.blockade || slate(event.tile).state.powerup())) {
                    slate(event.tile).state = Slate.State.empty;
                }
            } else {
                // had problems in the past
                if(event.tile == null) return;
                Call.onDeconstructFinish(event.tile, event.tile.block(), event.player.getID());
//                event.player.sendMessage("[scarlet] Don't build blocks!");
                event.player.applyEffect(StatusEffects.freezing, 180f);
                event.player.applyEffect(StatusEffects.tarred, 180f);
            }
        });

        // handle exploding bombs
        Events.on(BlockDestroyEvent.class, event -> {
            if(event.tile.block() != Blocks.thoriumReactor) return;
            if(stage.current() != Stage.playing) return;
            slate(event.tile).state = Slate.State.empty;

            Slate reactor = slate(event.tile);
            reactor.compass(Fire::create);

            Slate tmp;
            for(Direction direction : Direction.values()){
                int broken = 0;
                int max = Powerup.player(nukes.get(event.tile)).breaks;
                tmp = reactor.adjecent(direction);
                do{
                    if (tmp.state == Slate.State.wall) break;
                    if (tmp.state == Slate.State.empty || tmp.state.powerup() || tmp.state == Slate.State.pyroland) tmp.compass(Fire::create);

                    if (tmp.state == Slate.State.scrap){
                        tmp.destroy();
                        tmp.compass(Fire::create);
                        tmp.state = Slate.State.empty;
                        if(++broken >= max) break;
                    }

                    tmp = tmp.adjecent(direction);
                }while(true);
            }

            nukes.remove(event.tile);
        });

        netServer.assigner = (player, players) -> dead;
    }

    public static void reset(Runnable callback){

        nukes.clear();

        for(Player player : playerGroup){
            if(!player.isDead()) player.kill();
            player.setTeam(dead);
        }

        generator.seed();
        slates(slate -> {
            float delay = (slate.x + slate.y) / 20f;
            Timer.schedule(slate::destroy, delay);
            Timer.schedule(slate::place, delay + 0.25f);
        });

        Timer.schedule(() -> {

            Array<Spawn> spawns = generator.getSpawns();
            spawns.shuffle();
            for(Team team : teams){
                Player player = playerGroup.all().select(p -> p.getTeam() == dead).random();
                if(player == null) continue;
                player.setTeam(team);
                player.mech = Powerup.starter.mech;
                player.heal();
                player.dead = false;
                setLocationPosition(player, spawns.get(team.id - 2));
            }

            callback.run();
        }, ((slates.length + slates[0].length) / 20f) + 0.5f);
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("bomberman", "Begin hosting with the Bomberman gamemode.", args -> {
            logic.reset();
            Log.info("Generating map...");
            world.loadGenerator(generator = new BombermanGenerator());
            info("Map generated.");
            state.rules = rules.copy();
            logic.play();
            netServer.openServer();
        });
    }

    public boolean active(){
        return state.rules.tags.getBool("bomberman") && !state.is(State.menu);
    }

    private static void setLocationPosition(Player p, Position pos){
        Call.onPositionSet(p.con, pos.getX(), pos.getY());
        p.setNet(pos.getX(), pos.getY());
        p.set(pos.getX(), pos.getY());
    }
}
