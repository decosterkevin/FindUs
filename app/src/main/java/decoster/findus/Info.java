package decoster.findus;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kevin on 22.02.18.
 */

class Info {
    private long timestamp;
    private String id;
    private String userId;
    private LatLng position;
    public Info(long timestamp, String id, LatLng position, String userId) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.id = id;
        this.position = position;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
    }
    public JSONObject toJson(){

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


}
