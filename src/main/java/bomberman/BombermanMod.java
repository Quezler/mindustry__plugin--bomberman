package bomberman;

import arc.*;
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
import static bomberman.BombermanGenerator.pallete;
import static mindustry.Vars.*;

public class BombermanMod extends Plugin{
    private final Rules rules = new Rules();

    private BombermanGenerator generator;

    @Override
    public void init(){
        info("senpai bomberman <3");
        rules.tags.put("bomberman", "true");
        rules.infiniteResources = true;
        rules.canGameOver = false;

        //Todo: check for min 2 players and have a countdown (~ 10 seconds)
        //if game is already running -> spectator mode
        Events.on(PlayerJoin.class, event -> {
            if(!active()) return;

            event.player.kill();
            event.player.setTeam(Structs.random(teams));
            event.player.dead = false;

            //set location
            Call.onPositionSet(event.player.con, generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.setNet(generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.set(generator.spawns[0][0]*8, generator.spawns[0][1]*8);

            event.player.mech = Powerup.copper.mech;
            event.player.heal();
        });

        //block flying over walls
        Events.on(Trigger.update, () -> {
            if(!active()) return;

            for (Player p: playerGroup){
                if (!Structs.contains(teams, p.getTeam())) continue;

                Slate tmp = slate(tile(p));

                // player is death
                if(p.dead){
                    p.setTeam(dead);
                    Call.sendMessage(p.name + "[sky] died in an [accent]explosion...");
                }

                // player is on the same tile as a powerup
                if(tmp.state.powerup()){

                    Powerup tmp2 = Powerup.wall( tmp.center().block() );
                    if(tmp2 == null) return;
                    p.mech = tmp2.mech;
                    p.heal();
                    //remove powerup wall by building an airtile on top :thinking:
                    Call.onConstructFinish(tmp.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    tmp.state = Slate.State.empty;
                }

                if(!tmp.state.flyable()){
                    p.applyEffect(StatusEffects.freezing, 60f);
                    p.applyEffect(StatusEffects.tarred, 60f);
                    p.damage(2.5f);
                    if (p.dead){
                        p.setTeam(dead);
                        Call.sendMessage(p.name + "[sky] DIED[] (too much flying)");
                    }
                }

                if(p.isBoosting){
                    Slate over = slate(tile(p));

                    if(bombs.get(p.getTeam(), 0) >= 2) return; // 2 bombs per team max

                    if(over.state == Slate.State.empty){
                        over.state = Slate.State.bomb;
                        Call.onConstructFinish(over.center(), Blocks.thoriumReactor, p.id, (byte)0, p.getTeam(), true);

                        Powerup tmp3 = Powerup.player(p);
                        if(tmp3 != null) Timer.schedule(() -> Call.transferItemTo(Items.thorium, tmp3.thorium, p.x, p.y, over.center()), 0.25f);

                        bombs.getAndIncrement(p.getTeam(), 0, 1);
                    }
                }
            }
            //TODO: check if there is only one player alive
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if(!active()) return;

            if(event.breaking){
                if(event.tile.block() instanceof BuildBlock && (((BuildEntity)event.tile.ent()).previous == pallete.blockade || slate(event.tile).state.powerup())) {
                    slate(event.tile).state = Slate.State.empty;
                }
            } else {
                // had problems in the past
                if(event.tile == null) return;
                Call.onDeconstructFinish(event.tile, event.tile.block(), event.player.getID());
                event.player.sendMessage("[scarlet] Don't build blocks!");

                event.player.applyEffect(StatusEffects.freezing, 180f);
                event.player.applyEffect(StatusEffects.tarred, 180f);
                event.player.damage(60f);
                if (event.player.dead){
                    event.player.setTeam(dead);
                    Call.sendMessage(event.player.name + "[sky] DIED[] (too much building)");
                }
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            slate(event.tile).state = Slate.State.empty;
            if(event.tile.block() != Blocks.thoriumReactor) return;

            bombs.getAndIncrement(event.tile.getTeam(), 0, -1);
            Slate reactor = slate(event.tile);
            reactor.compass(Fire::create);

            Slate tmp;
            for(Direction direction : Direction.values()){
                tmp = reactor.adjecent(direction);
                do{
                    if (tmp.state == Slate.State.wall) break;
                    if (tmp.state == Slate.State.empty || tmp.state.powerup()) tmp.compass(Fire::create);

                    if (tmp.state == Slate.State.scrap){
                        omeowamoushindeiru(tmp);
                        tmp.compass(Fire::create);
                        tmp.state = Slate.State.empty;
                        break;
                    }

                    tmp = tmp.adjecent(direction);
                }while(true);
            }
        });

        //what does this do - dunno, it was there in hexedmod.
        netServer.assigner = (player, players) -> Team.sharded;
    }

    private void omeowamoushindeiru(Slate slate){
        Core.app.post(() -> {
            slate.center().entity.onDeath();
            slate.center().removeNet();
        });
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
}
