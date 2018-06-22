package com.englebertlai.multikwad;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FlightModeActivity extends AppCompatActivity {
    Context flightmode_activity_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flightmode);

        flightmode_activity_context = this;

        setup();
    }

    void setup() {
        // Setting up buttons
        Button button_exit = (Button) findViewById(R.id.button_back);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
