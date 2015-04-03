package pss.rookscore.core.events;


import pss.rookscore.core.model.GameStateModel;

public class GameOverEvent {

    private final GameStateModel mGameModel;

    public GameOverEvent(GameStateModel gameModel) {
        mGameModel = gameModel;
    }

    public GameStateModel getGameModel() {
        return mGameModel;
    }
}
