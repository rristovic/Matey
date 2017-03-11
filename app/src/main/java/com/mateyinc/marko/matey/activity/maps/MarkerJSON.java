package com.mateyinc.marko.matey.activity.maps;


import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Class used for serialising and deserialising list of markers
 */
public class MarkerJSON {

    private static final String TAG = MarkerJSON.class.getSimpleName();

    /**
     * Parameters for marker for intent extra containing markers
     */
    public static final String MARKER_TITLE = "description";
    public static final String MARKER_LAT = "latt";
    public static final String MARKER_LONG = "longt";


    public static String serialiseMarkers(List<Marker> list) {

        if (list == null || list.size() == 0)
            return "";

        JSONArray array = new JSONArray();
        for (Marker m :
                list) {
            try {
                JSONObject ob = new JSONObject();
                ob.put(MARKER_LAT, m.getPosition().latitude);
                ob.put(MARKER_LONG, m.getPosition().longitude);
                ob.put(MARKER_TITLE, m.getTitle());
                array.put(ob);
            } catch (JSONException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
        return array.toString();
    }

    /**
     * Method for asking if provided string is marker json object
     *
     * @param marker marker string
     * @return true if this is marker
     */
    public static boolean isMarker(String marker) {
        return marker != null && !marker.isEmpty()
                && marker.startsWith("{\"" + MARKER_LAT);
    }
}
