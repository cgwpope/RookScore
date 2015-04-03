package pss.rookscore.core.model;


import java.io.Serializable;

public class Player implements Serializable, Comparable<Player> {
    private String mFirstname;
    private String mLastname;


    public Player(String firstname, String lastname){
        mFirstname = firstname;
        mLastname = lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (mFirstname != null ? !mFirstname.equals(player.mFirstname) : player.mFirstname != null)
            return false;
        if (mLastname != null ? !mLastname.equals(player.mLastname) : player.mLastname != null)
            return false;

        return true;
    }

    public int getId() {
        return hashCode();
    }


    @Override
    public int hashCode() {
        int result = mFirstname != null ? mFirstname.hashCode() : 0;
        result = 31 * result + (mLastname != null ? mLastname.hashCode() : 0);
        return result;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public String getLastname() {
        return mLastname;
    }

    public String toString() {
        return  mFirstname + " " + mLastname;
    }

    @Override
    public int compareTo(Player another) {
        if(another == null){
            return 1;
        } else {
            return toString().compareTo(another.toString());
        }
    }
}
