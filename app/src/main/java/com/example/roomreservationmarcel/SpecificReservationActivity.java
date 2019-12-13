package com.example.roomreservationmarcel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.roomreservationmarcel.Classes.Reservation;
import com.example.roomreservationmarcel.Classes.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpecificReservationActivity extends AppCompatActivity {

    public static final String RESERVATION = "RESERVATION";

    private Room myRoom;

    private Reservation reservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificreservation);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Reservation information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        reservation = (Reservation) intent.getSerializableExtra(RESERVATION);

        TextView purposeView = (TextView) findViewById(R.id.ReservationPurpose);
        purposeView.setText("Purpose: " + reservation.getPurpose());

        TextView fromTimeView = (TextView) findViewById(R.id.ReservationFromTime);
        fromTimeView.setText("From: " + new Date(reservation.getFromTime() * 1000));

        TextView toTimeView = (TextView) findViewById(R.id.ReservationToTime);
        toTimeView.setText("To: " + new Date(reservation.getToTime() * 1000));

    }


    @Override
    protected void onStart() {
        super.onStart();


        getReservationsForSpecificRoom(reservation.getRoomId());
    }

    private void getReservationsForSpecificRoom(int roomId)
    {
        String url = "http://anbo-roomreservationv3.azurewebsites.net/api/rooms/" + roomId;
        OkHttpClient client = new OkHttpClient();
        Request.Builder request = new Request.Builder();
        Request build = request.url(url).build();

        client.newCall(build).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {


            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {

                if (!response.isSuccessful())
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                } else {
                    final String jsonString = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new GsonBuilder().create();


                            final Room room = gson.fromJson(jsonString, Room.class);
                            myRoom = room;
                            TextView roomName = (TextView) findViewById(R.id.ReservationRoomName);
                            roomName.setText("Room name: " + myRoom.getName());

                            TextView roomSize = (TextView) findViewById(R.id.ReservationRoomSize);
                            roomSize.setText("Room size: " + myRoom.getCapacity());

                        }
                    });

                }


            }


        });
    }


    public void DeleteReservation(View view) {

        final String url = "http://anbo-roomreservationv3.azurewebsites.net/api/reservations/" + reservation.getId();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView messageView = findViewById(R.id.onDeleteFailure);
                        messageView.setText(ex.getMessage());

                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView messageView = findViewById(R.id.onDeleteFailure);
                        if (response.isSuccessful()) {
                            messageView.setText("Reservation deleted");
                            finish();
                        } else {
                            messageView.setText(url + "\n" + response.code() + " " + response.message());
                        }
                    }
                });
            }
        });
        finish();
    }
}
