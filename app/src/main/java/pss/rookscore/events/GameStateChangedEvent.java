package pss.rookscore.events;

import pss.rookscore.model.GameStateModel;

public class GameStateChangedEvent {
    
    public final GameStateModel mModel;

    public GameStateChangedEvent(GameStateModel model) {
        super();
        mModel = model;
    }
    
    
}
