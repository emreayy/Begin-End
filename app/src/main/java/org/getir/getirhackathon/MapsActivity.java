package org.getir.getirhackathon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Socket socket;
    private CourierHolder courierHolder;
    private boolean reached = false;

    private LatLng position = new LatLng(41.0786219f, 29.0222581f);

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
        socket.on("courierPositions", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.clear();
                        myLocationMarker();
                        Timer timer = new Timer();
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (!reached)
                                requestNewPos();
                            }
                        };

                        timer.schedule(timerTask, 300);
                    }
                });
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

        }).on("reached", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                reached = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapsActivity.this, "Siparişiniz ulaşmıştır", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        });
        socket.connect();




    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //41.0786219,29.0222581
        myLocationMarker();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13));

    }

    private void myLocationMarker () {
        map.addMarker(new MarkerOptions().position(position).title("Here!"));
    }

    public void order(View view) {
        reached = false;
        Toast.makeText(this, "Siparişiniz yola çıkmıştır", Toast.LENGTH_SHORT).show();

        Timer t = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                requestNewPos();
            }
        };

        t.schedule(task, 3000);
    }

    private void requestNewPos() {
        if (socket != null && socket.connected()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", 41.0786219f);
                jsonObject.put("longitude", 29.0222581f);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("newpos",jsonObject);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MapsActivity.this, "Sunucuya bağlanılmadı", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
