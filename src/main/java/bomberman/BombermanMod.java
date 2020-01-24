package bomberman;

import arc.*;
import arc.func.*;
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
import static bomberman.BombermanGenerator.pallete;
import static mindustry.Vars.*;

public class BombermanMod extends Plugin{
    private final Rules rules = new Rules();

    private BombermanGenerator generator;
    //game started
    private boolean started = false;
    private boolean countdown = false;

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

            event.player.dead = false;
            setLocationTile(player, world.width() / 2, world.height() / 2);

//            event.player.kill();
//            event.player.setTeam(Structs.random(teams));
//            event.player.mech = Powerup.copper.mech;
//            event.player.dead = false;
//            setLocationTile(event.player, generator.spawns[0][0], generator.spawns[0][1]);

//            event.player.dead = true;
//
//            //playerGroup.size() will update after this event!
//            //too many players
//            if (playerGroup.size() > 4 || started){
//                event.player.setTeam(dead);
//                setLocationTile(event.player, 2, 2);
//                event.player.sendMessage("\nThe game has already started. You entered [accent]spectator[] mode.\n");
//            } else if (playerGroup.size() < 1){
//                Call.onInfoMessage("Minimum 2 players are required to play [sky]Bomberman.[]\nThe countdown will start if a second player joins.");
//            } else if (countdown) {
//                //min 2 players, let them now that the game will start soon
//                event.player.sendMessage("The game will start [accent]very[] soon.");
//            } else {
//                Call.sendMessage("Bomberman will start in 10 seconds...");
//                //assign players to team in 10 seconds and start the game
//                this.countdown = true;
//                Timer.schedule(() -> startGame(), 10f);
//            }

            /*
            //set location
            Call.onPositionSet(event.player.con, generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.setNet(generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.set(generator.spawns[0][0]*8, generator.spawns[0][1]*8);

            event.player.mech = Powerup.copper.mech;
            event.player.heal();*/
        });


        Events.on(Trigger.update, () -> {
            if(!active()) return;

            if(phase != Phase.resetting && playerGroup.size() > 0 && playerGroup.count(p -> !p.isDead() && p.getTeam() != dead) == 0){
                phase = Phase.resetting;
                Timer.schedule(() -> reset(() -> phase = Phase.playing), 1.5f);
            }
        });

        Events.on(Trigger.update, () -> {
            if(!active()) return;
//            if(!started) return;

            for (Player p: playerGroup){
                if (!Structs.contains(teams, p.getTeam())) continue;

                Slate tmp = slate(tile(p));

//                // player is death
//                if(p.dead){
//                    p.setTeam(dead);
//                    Call.sendMessage(p.name + "[sky] died in an [accent]explosion...");
//                }

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
//                    if (p.dead){
//                        p.setTeam(dead);
//                        Call.sendMessage(p.name + "[sky] DIED[] (too much flying)");
//                    }
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
//            //TODO: check if there is only one player alive
//            if(playerGroup.count(p -> Structs.contains(teams, p.getTeam())) <= 1){ //potential == 0
//                Call.onInfoMessage("[accent] --- Game Ended --- []\n" + playerGroup.find(p -> !p.dead).name + "[] won!\n\n[sky]The map will reset in 10 seconds.");
//                //TODO: reset or change maps after 10 seconds and maybe kick all players.
//                Call.onPlayerDeath(playerGroup.find(p -> !p.dead)); //remove
//                this.started = false;
//                this.countdown = false;
//            }

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
                event.player.damage(60f);
                if (event.player.dead){
                    event.player.setTeam(dead);
                    Call.sendMessage(event.player.name + "[sky] DIED[] (too much building)");
                }
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
                tmp = reactor.adjecent(direction);
                do{
                    if (tmp.state == Slate.State.wall) break;
                    if (tmp.state == Slate.State.empty || tmp.state.powerup()) tmp.compass(Fire::create);

                    if (tmp.state == Slate.State.scrap){
                        tmp.destroy();
                        tmp.compass(Fire::create);
                        tmp.state = Slate.State.empty;
                        break;
                    }

                    tmp = tmp.adjecent(direction);
                }while(true);
            }
        });

        netServer.assigner = (player, players) -> dead;
    }

    public void reset(Runnable callback){

        bombs.clear();

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

            for(Team team : teams){
                Player player = playerGroup.all().select(p -> p.getTeam() == dead).random();
                if(player == null) continue;
                player.setTeam(team);
                player.mech = Powerup.copper.mech;
                player.heal();
                player.dead = false;
                setLocationTile(player, generator.spawns[team.id - 2][0], generator.spawns[team.id - 2][1]);
            }

            for(Player player : playerGroup.all().select(p -> p.getTeam() == dead)){
                player.dead = false;
                player.mech = Mechs.dart;
                setLocationTile(player, world.width() / 2, world.height() / 2);
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

        handler.register("reset", "Test reset animation.", args -> {
            reset(() -> Log.info("Reset complete!"));
        });
    }

    public boolean active(){
        return state.rules.tags.getBool("bomberman") && !state.is(State.menu);
    }

    private void setLocationTile(Player p, int x, int y){
        Call.onPositionSet(p.con, x * tilesize, y * tilesize);
        p.setNet(x * tilesize, y * tilesize);
        p.set(x * tilesize, y * tilesize);
    }
}
