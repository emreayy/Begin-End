package org.getir.getirhackathon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Socket socket;
    private CourierHolder courierHolder;

    //private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            socket = IO.socket("https://appgetir.herokuapp.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject json = new JSONObject();
                try {
                    json.put("mesaj","Telefon baglandi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("yenimesaj", json);
            }

        }).on("courierPositions", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                courierHolder = new Gson().fromJson(jsonObject.toString(), CourierHolder.class);

                for (Courier courier: courierHolder.getCourierList()) {
                    final MarkerOptions marker = new MarkerOptions().position(new LatLng(courier.getLatitude(), courier.getLongitude())).title("Your courier");
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.getir));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.addMarker(marker);
                        }
                    });
                }



            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        });
        socket.connect();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.




    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        //41.0786219,29.0222581

        LatLng position = new LatLng(41.0786219f, 29.0222581f);
        map.addMarker(new MarkerOptions().position(position).title("Here!"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }
}
