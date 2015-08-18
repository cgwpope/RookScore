package pss.rookscore.core.webapi;

import ca.cgwpope.jsonobjectwrapper.IJSONObjectWrapper;

/**
 * Created by t890428 on 2015-04-04.


JSON Backing:
 {
 "caller":4,
 "partners": [ 20, 22 ],
 "opponents": [ 21, 19, 23 ],
 "points_bid": 150,
 "points_made": 180,
 "hand_number": 1
 }



 */
public interface RemoteRound extends IJSONObjectWrapper {

    public int getCaller();
    public void setCaller(int callerId);

    public Integer[] getPartners();
    public void setPartners(Integer partnerPlayerIds[]);

    public Integer[] getOpponents();
    public void setOpponents(Integer opponentPlayerIds[]);

    public int getPointsBid();
    public void setPointsBid(int pointsBid);

    public int getPointsMade();
    public void setPointsMade(int pointsMade);

    public int getHandNumber();
    public void setHandNumber(int handNumber);

}
