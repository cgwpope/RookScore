package pss.rookscore.core.webapi;

import ca.cgwpope.jsonobjectwrapper.IJSONObjectWrapper;

public interface RemoteGameScore extends IJSONObjectWrapper  {
    public int getPlayer();
    public void setPlayer(int playerId);

    public int getScore();
    public void setScore(int score);

    public boolean getMadeBid();
    public void setMadeBid(boolean madeBid);

}
