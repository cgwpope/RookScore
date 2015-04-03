package pss.rookscore.core.webapi;

import ca.cgwpope.jsonobjectwrapper.IJSONObjectWrapper;


public interface RemoteGame extends IJSONObjectWrapper {

    public String getEnteredDate();
    public void setEnteredDate(String enteredDate);

    public String getPlayedDate();
    public void setPlayedDate(String playedDate);

    public RemoteGameScore[] getScores();
    public void setScores(RemoteGameScore scores[]);


    public String[] getBids();
    public void setBids(String[] bids);
}
