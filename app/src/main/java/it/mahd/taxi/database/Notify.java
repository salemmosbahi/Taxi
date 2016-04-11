package it.mahd.taxi.database;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import it.mahd.taxi.util.Controllers;
import it.mahd.taxi.util.SocketIO;

/**
 * Created by salem on 3/25/16.
 */
public class Notify {
    Controllers conf = new Controllers();
    Socket socket = SocketIO.getInstance();
    private Boolean reclamation = false;
    private Fragment fragment;

    public Boolean reclamationNotify(Fragment frag) {
        socket.connect();
        fragment = frag;
        socket.on(conf.io_reclamation, handleIncomingMessages);
        return reclamation;
    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener(){
        public void call(final Object... args){
            fragment.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Boolean refresh;
                    try {
                        refresh = data.getBoolean(conf.tag_refresh);
                        reclamation = refresh;
                    } catch (JSONException e) {
                    }
                }
            });
        }
    };
}
