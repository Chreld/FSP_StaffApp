package fsp.staffapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fsp.staffapp.network.ApiClient;
import fsp.staffapp.template.Template;

public class OccupiedParkingSpotsPage extends Template {

    private EditText spotInput;
    private Button searchBtn;
    private Button reportBtn;
    private TextView result;

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.activity_occupied_parking_spots);

        spotInput = findViewById(R.id.spotInput);
        searchBtn = findViewById(R.id.searchBtn);
        result = findViewById(R.id.result);
        reportBtn = findViewById(R.id.reportBtn);

        reportBtn.setVisibility(View.GONE);

        searchBtn.setOnClickListener(view -> searchParkingSpot());

        reportBtn.setOnClickListener(view -> {

            String parkingSpot = spotInput.getText()
                    .toString().trim().toUpperCase();

            Intent intent = new Intent(OccupiedParkingSpotsPage.this, ReportIllegalParkingPage.class);

            intent.putExtra("parkingSpot", parkingSpot);
            startActivity(intent);
        });
    }

    private void searchParkingSpot() {

        String parkingSpot = spotInput.getText().toString().trim().toUpperCase();

        if (parkingSpot.isEmpty()) {
            result.setText(R.string.enter_parking_spot);
            result.setVisibility(View.VISIBLE);
            reportBtn.setVisibility(View.GONE);
            return;
        }

        result.setText("Checking parking spot...");
        result.setVisibility(View.VISIBLE);
        reportBtn.setVisibility(View.GONE);
        searchBtn.setEnabled(false);

        // Ask ApiClient to retrieve the expected license plate
        ApiClient.getLicensePlate(parkingSpot, new ApiClient.ApiCallback() {

            @Override
            public void onSuccess(String response) {

                String licensePlate = response.trim();

                if (licensePlate.isEmpty() || licensePlate.equalsIgnoreCase("null")) {

                    result.setText(R.string.no_car_expected);
                    reportBtn.setVisibility(View.VISIBLE);

                } else {

                    result.setText(getString(
                            R.string.expected_license_plate,
                            licensePlate
                    ));

                    reportBtn.setVisibility(View.GONE);
                }

                searchBtn.setEnabled(true);
            }

            @Override
            public void onError(String error) {

                result.setText("Could not retrieve parking spot.\n" + error
                );

                result.setVisibility(View.VISIBLE);
                reportBtn.setVisibility(View.GONE);
                searchBtn.setEnabled(true);
            }
        });
    }
}
