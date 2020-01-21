package bomberman;

import mindustry.net.Administration.*;

import static bomberman.BombermanGenerator.pallete;

public class BombermanActionFilter implements ActionFilter{
    @Override
    public boolean allow(PlayerAction playerAction){
        if(playerAction.type == ActionType.breakBlock && playerAction.block == pallete.blockade) return false;

        return true;
    }
}
