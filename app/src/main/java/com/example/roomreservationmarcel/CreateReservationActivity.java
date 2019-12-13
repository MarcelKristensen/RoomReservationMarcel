package com.example.roomreservationmarcel;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.roomreservationmarcel.Classes.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateReservationActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView ShowDateView;
    private TextView FromTime;
    private TextView ToTime;
    private TextView OnFailure;
    private EditText Purpose;
    private int roomId;
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private int selectedFromHour;
    private int selectedFromMinute;
    private int selectedToHour;
    private int selectedToMinute;
    private Button fromTimeButton;
    private Button toTimeButton;
    private Button reserveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createreservations);
        ShowDateView = findViewById(R.id.ShowDateView);
        FromTime = findViewById(R.id.FromTime);
        ToTime = findViewById(R.id.ToTime);
        OnFailure = findViewById(R.id.OnFailure);
        setFromTime();
        setToTime();
        Purpose = findViewById(R.id.PurposeText);

        reserveButton = findViewById(R.id.reserveButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle("Create reservation");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        roomId = intent.getIntExtra("roomId", -1);
    }

    public void ChooseDate(View view) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePickerDialog.OnDateSetListener) this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        datePickerDialog.show();
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day_of_month) {
        selectedYear = year;
        selectedMonth = month;
        selectedDay = day_of_month;
        Integer rightMonth = month + 1;
        String date = "Date: " + selectedDay + "/" + rightMonth + "/" + selectedYear;
        ShowDateView.setText(date);
        fromTimeButton.setVisibility(View.VISIBLE);
    }

    private Long getTime(int hour, int minute) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, selectedYear);
        cal.set(Calendar.MONTH, selectedMonth);
        cal.set(Calendar.DAY_OF_MONTH, selectedDay);

        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);

        Long calMil = cal.getTimeInMillis();
        return calMil / 1000;
    }


    private void setFromTime() {
        fromTimeButton = (Button) findViewById(R.id.FromTimeButton);
        fromTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        selectedFromHour = hour;
                        selectedFromMinute = minute;

                        String h;
                        String min;
                        if (selectedFromHour < 10) {
                            h = "0" + hour;
                        } else
                            h = "" + hour;

                        if (selectedFromMinute < 10) {
                            min = "0" + minute;
                        } else
                            min = "" + minute;


                        FromTime.setText("From: " + h + ":" + min);
                        toTimeButton.setVisibility(View.VISIBLE);
                    }
                };

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        CreateReservationActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog,
                        onTimeSetListener, Calendar.getInstance().get(Calendar.HOUR),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true);

                timePickerDialog.show();


            }
        });
    }

    private void setToTime() {
        toTimeButton = (Button) findViewById(R.id.ToTimeButton);
        toTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        selectedToHour = hour;
                        selectedToMinute = minute;

                        String h;
                        String min;
                        if (selectedToHour < 10) {
                            h = "0" + hour;
                        } else
                            h = "" + hour;

                        if (selectedToMinute < 10) {
                            min = "0" + minute;
                        } else
                            min = "" + minute;


                        ToTime.setText("To: " + h + ":" + min);
                        reserveButton.setVisibility(View.VISIBLE);
                    }
                };

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        CreateReservationActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog,
                        onTimeSetListener, Calendar.getInstance().get(Calendar.HOUR),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true);

                timePickerDialog.show();

            }
        });
    }

    private void postReservation(){

        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        Long fromTime = getTime(selectedFromHour, selectedFromMinute);
        Long toTime = getTime(selectedToHour, selectedToMinute);
        String userId = FirebaseAuth.getInstance().getUid();
        String purpose = Purpose.getText().toString();
        Reservation newRes = new Reservation(fromTime, toTime, userId, purpose, roomId);
        String jsonReservation = new Gson().toJson(newRes);


        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE, jsonReservation);
        Request request = new Request.Builder()
                .url("http://anbo-roomreservationv3.azurewebsites.net/api/Reservations")
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isSuccessful()) {
                            String errorBody = null;
                            try {
                                errorBody = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            OnFailure.setBackgroundColor(Color.RED);
                            OnFailure.setText("Error: " + errorBody);
                        }
                        else {
                            OnFailure.setBackgroundColor(Color.GREEN);
                            OnFailure.setText("Room reserved");
                            finish();
                        }
                    }
                });

            }
        });
    }

    public void ReserveRoom(View view) {

        String purposeField = Purpose.getText().toString().trim();

        if (TextUtils.isEmpty(purposeField))
        {
            OnFailure.setText("Need to declare a purpose");
            return;
        }
        else if (selectedFromHour * 60 + selectedFromMinute > selectedToHour * 60 + selectedToMinute)
        {
            OnFailure.setText("Invalid time, check your From and To time");
            return;
        }else
            postReservation();

    }
}