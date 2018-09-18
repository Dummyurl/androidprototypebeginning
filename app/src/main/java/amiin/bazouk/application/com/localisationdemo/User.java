package amiin.bazouk.application.com.localisationdemo;

import android.support.annotation.NonNull;

public class User implements Comparable<User>{
    private String username;
    private double earnings;
    private double expenses;
    private MarkerSold markerBought;
    private MarkerSold markerSold;
    private boolean isBuyOn = false;

    User(String username)
    {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public double getEarnings()
    {
        return earnings;
    }

    public double getExpenses()
    {
        return expenses;
    }

    public MarkerSold getMarkerBought() {
        return markerBought;
    }

    public MarkerSold getMarkerSold() {
        return markerSold;
    }

    public boolean isBuyOn() {
        return isBuyOn;
    }

    public void setMarkerBought(MarkerSold markerBought)
    {
        this.markerBought = markerBought;
    }

    public void setMarkerSold(MarkerSold markerSold)
    {
        this.markerSold = markerSold;
    }

    public void setExpenses(double expenses)
    {
        this.expenses= expenses;
    }

    public void addEarnings(double valueSpent) {
        earnings+=valueSpent;
    }

    public void soustractEarnings(double earnings)
    {
        this.earnings -= earnings;
    }

    public void soustractExpenses(double expenses)
    {
        this.expenses -= expenses;
    }

    public void setBuyOn(boolean isBuyOn) {
        this.isBuyOn = isBuyOn;
    }

    @Override
    public int compareTo(@NonNull User user) {
        if(username.equals(user.username))
        {
            return 0;
        }
        return 1;
    }
}

