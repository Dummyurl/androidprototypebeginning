package amiin.bazouk.application.com.localisationdemo;

import android.support.annotation.NonNull;

public class User implements Comparable<User>{

    private String username;
    private double minPrice;
    private double maxPrice;
    private MarkerSold markerBought;
    private MarkerSold markerSold;
    private double earnings;
    private double expenses;

    User(String username, double minPrice, double maxPrice)
    {
        this.username = username;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getUsername() {
        return username;
    }

    public double getMinPrice()
    {
        return minPrice;
    }

    public double getMaxPrice()
    {
        return maxPrice;
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

    public void setMarkerBought(MarkerSold markerBought)
    {
        this.markerBought = markerBought;
    }

    public void setMarkerSold(MarkerSold markerSold)
    {
        this.markerSold = markerSold;
    }

    public void addEarnings(double earnings)
    {
        this.earnings += earnings;
    }

    public void addExpenses(double expenses)
    {
        this.expenses += expenses;
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

