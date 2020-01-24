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
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.BuildBlock.*;

import static arc.util.Log.info;
import static bomberman.Bomberman.*;
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
        rules.playerDamageMultiplier = 0f;

        //Todo: check for min 2 players and have a countdown (~ 10 seconds)
        //if game is already running -> spectator mode
        Events.on(PlayerJoin.class, event -> {
            if(!active()) return;

            event.player.kill();
            event.player.dead = true;
            event.player.setTeam(dead);

            if(phase == Phase.playing) {
                Call.onInfoToast(event.player.con,"\nThe game has already started. You entered [accent]spectator[] mode.\n", 10f);
            }else if(phase == Phase.waiting && playerGroup.size()<1){
                Call.onInfoToast(event.player.con, "Minimum 2 players are required to play [sky]Bomberman.[]\nThe game will start if a second player joins.", 10f);
            }
        });


        Events.on(Trigger.update, () -> {
            if(!active()) return;

            // reset the map when there are no alive players
            if(phase != Phase.resetting && playerGroup.size() > 0 && playerGroup.count(p -> !p.isDead()) == 0){
                if(playerGroup.size() < 2 && phase != Phase.waiting){
                    Call.onInfoMessage("[scarlet]Not enough players to start a new game...");
                    phase = Phase.waiting;
                } else if(playerGroup.size() > 1) {
                    Call.onInfoToast("[sky]Resetting the map!", 5f);
                    phase = Phase.resetting;
                    Timer.schedule(() -> reset(() -> phase = Phase.playing), 1.5f);
                }
            }

            // if there is only one player/team standing, slowly kill it to prevent a deadlock
            if(phase != Phase.resetting && playerGroup.size() > 0 && playerGroup.count(p -> !p.isDead()) == 1){
                if(phase != Phase.ending){
                    Call.onInfoToast("[accent] --- Game Ended --- []\n" + playerGroup.find(p -> !p.dead).name + "[] won!\n\n[sky]The map will reset soon.", 5f);
                    playerGroup.find(p -> !p.dead).heal(); //small delay
                    phase = Phase.ending;
                }
                playerGroup.all().select(p -> !p.isDead()).each(p -> p.applyEffect(StatusEffects.corroded, 60f));
            }
        });

        Events.on(Trigger.update, () -> {
            if(!active()) return;

            if(phase != Phase.playing && phase != Phase.ending) return;

            for (Player p: playerGroup){
                if (!Structs.contains(teams, p.getTeam())) continue;

                Slate tmp = slate(tile(p));

                // player is death
                if(p.dead){
                    p.setTeam(dead);
                    Call.sendMessage(p.name + "[sky] died...");
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

                // pallete switcher
                if(tmp.state == Slate.State.pyroland){
                    pallete = Structs.random(Pallete.values());

                    for(int x = 0; x < world.width(); x++){
                        for(int y = 0; y < world.height(); y++){
                            world.getTiles()[x][y].setFloor(pallete.floor);
                        }
                    }

                    slates(slate -> {
                        if(slate.center().block() != Blocks.thoriumReactor) slate.place();
                    }); // fixme: breaks/freezes currently placed nukes
                    playerGroup.all().each(syncer -> netServer.clientCommands.handleMessage("/sync", syncer));
                    Call.onConstructFinish(tmp.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    tmp.state = Slate.State.empty;
                }

                if(!tmp.state.flyable()){
                    p.applyEffect(StatusEffects.freezing, 60f);
                    p.applyEffect(StatusEffects.tarred, 60f);
                    p.damage(2.5f);
                }

                if(p.isBoosting){
                    Slate over = slate(tile(p));

                    if(bombs.get(p.getTeam(), 0) >= bombs(p.getTeam())) return;

                    if(over.state == Slate.State.empty){
                        over.state = Slate.State.bomb;
                        Call.onConstructFinish(over.center(), Blocks.thoriumReactor, p.id, (byte)0, p.getTeam(), true);

                        Timer.schedule(() -> Call.transferItemTo(Items.thorium, Powerup.player(p).thorium, p.x, p.y, over.center()), 0.25f);

                        bombs.getAndIncrement(p.getTeam(), 0, 1);
                        nukes.put(over.center(), p);
                    }
                }
            }

        });

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
                event.player.sendMessage("[scarlet] Don't build blocks!");
                event.player.applyEffect(StatusEffects.freezing, 180f);
                event.player.applyEffect(StatusEffects.tarred, 180f);
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if(event.tile.block() != Blocks.thoriumReactor) return;
            if(phase != Phase.playing) return;
            slate(event.tile).state = Slate.State.empty;

            bombs.getAndIncrement(event.tile.getTeam(), 0, -1);
            Slate reactor = slate(event.tile);
            reactor.compass(Fire::create);

            Slate tmp;
            for(Direction direction : Direction.values()){
                int broken = 0;
                int max = Powerup.player(nukes.get(event.tile)).breaks;
                tmp = reactor.adjecent(direction);
                do{
                    if (tmp.state == Slate.State.wall) break;
                    if (tmp.state == Slate.State.empty || tmp.state.powerup()) tmp.compass(Fire::create);

                    if (tmp.state == Slate.State.scrap){
                        tmp.destroy();
                        tmp.compass(Fire::create);
                        tmp.state = Slate.State.empty;
                        if(++broken >= max) break;
                    }

                    tmp = tmp.adjecent(direction);
                }while(true);
            }
        });

        netServer.assigner = (player, players) -> dead;
    }

    public void reset(Runnable callback){

        bombs.clear();
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

//    private void startGame(){
//        if(playerGroup.size() < 2){
//            //abort -- player left
//            Call.sendMessage("[scarlet]Not enough players to start a game...");
//            this.countdown = false;
//            return;
//        }
//        for (int index = 0; index < playerGroup.size(); index++){
//            if (index == 4) break;
//            Player p = playerGroup.all().get(index);
//            p.dead = false;
//            setLocationTile(p, generator.spawns[index][0], generator.spawns[index][1]);
//            p.setTeam(teams[index]);
//            p.mech = Powerup.copper.mech;
//            p.heal();
//        }
//        started = true;
//        Call.sendMessage("[green]Game Started[]\n[accent]Dash[] to place a nuke.");
//    }


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

    private void setLocationPosition(Player p, Position pos){
        Call.onPositionSet(p.con, pos.getX(), pos.getY());
        p.setNet(pos.getX(), pos.getY());
        p.set(pos.getX(), pos.getY());
    }
}
