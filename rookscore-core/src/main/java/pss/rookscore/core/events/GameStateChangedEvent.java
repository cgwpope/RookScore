package pss.rookscore.core.events;

import pss.rookscore.core.model.GameStateModel;

public class GameStateChangedEvent {
    
    public final GameStateModel mModel;

    public GameStateChangedEvent(GameStateModel model) {
        super();
        mModel = model;
    }
    
    
}
