package fsp.staffapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.activity_menu);

        Button usedSpotsBtn = findViewById(R.id.usedSpotsButton);
        Button reportIllegalBtn = findViewById(R.id.reportIllegalButton);

        usedSpotsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenuPage.this, OccupiedParkingSpotsPage.class);
            startActivity(intent);
        });

        reportIllegalBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenuPage.this, ReportIllegalParkingPage.class);
            startActivity(intent);
        });
    }
}
