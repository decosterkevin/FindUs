package decoster.findus.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import decoster.findus.utility.Utilities;

/**
 * Created by kevin on 22.02.18.
 */

public class Peer {
    private long timestamp;
    private String id;
    private String userId;
    private LatLng position;
    private String status;
    private Marker marker;

    public Peer(String id, String userId) {
        this.userId = userId;
        this.id = id;
    }

    public Peer(String id, String userId, LatLng position, long timestamp, String status, Marker marker) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.id = id;
        this.position = position;
        this.status = status;
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
        this.marker.setSnippet("last update " + Utilities.getDateToString(timestamp));
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
        marker.setPosition(position);
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();

        try {
            json.put("id", id);
            json.put("userId", userId);
            json.put("lat", position.latitude);
            json.put("lon", position.longitude);
            json.put("timestamp", timestamp);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void updateLocation(Location pos) {
        this.position = new LatLng(pos.getLatitude(), pos.getLongitude());
        this.timestamp = pos.getTime();
    }
}
