package amiin.bazouk.application.com.localisationdemo;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerSold {

    private User ownerMarker;
    private Marker marker;
    private LatLng latLng;
    private double price;
    private double volume;
    private List<User> buyersMarker;

    MarkerSold(User ownerMarker, Marker marker, LatLng latLng, double price, double volume)
    {
        this.ownerMarker = ownerMarker;
        this.marker = marker;
        this.latLng = latLng;
        this.price = price;
        this.volume = volume;
        buyersMarker = new ArrayList<>();
    }

    public User getOwnerMarker() {
        return ownerMarker;
    }

    public Marker getMarker()
    {
        return marker;
    }

    public LatLng getLatLng()
    {
        return latLng;
    }

    public double getPrice()
    {
        return price;
    }

    public double getVolume()
    {
        return volume;
    }

    public List<User> getBuyersMarker() {
        return buyersMarker;
    }

    public void setMarker(Marker marker)
    {
        this.marker = marker;
    }

    public void addBuyerMarker(User newBuyer)
    {
        buyersMarker.add(newBuyer);
    }
}
