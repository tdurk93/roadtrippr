package roadtrippr.roadtrippr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {

    Button saveButton;
    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;
    NumberPicker timeWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide soft keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        saveButton = (Button) findViewById(R.id.saveButton);

        final SharedPreferences sharedPref = getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        String favRestaurantsString = sharedPref.getString("favRestaurants", "");
        String favRestaurantsTypesString = sharedPref.getString("favRestaurantsTypes", "");
        String noRestaurantsString = sharedPref.getString("noRestaurants", "");
        String timeWindowString = sharedPref.getString("timeWindow", "");

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.restaurants_field);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.restaurant_types_field);
        noRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.unacceptable_restaurants_field);

        timeWindow = (NumberPicker) findViewById(R.id.eating_window_picker);
        timeWindow.setMinValue(0);
        timeWindow.setMaxValue(11);
        timeWindow.setDisplayedValues(new String[]{"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.restaurants, android.R.layout.simple_list_item_1);
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this, R.array.restaurants_types, android.R.layout.simple_list_item_1);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.times, android.R.layout.simple_spinner_item);

        favRestaurants.setAdapter(adapter);
        favRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        favTypes.setAdapter(adapter2);
        favTypes.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        noRestaurants.setAdapter(adapter);
        noRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = favRestaurants.getText().toString();
                String str2 = favTypes.getText().toString();
                String str3 = noRestaurants.getText().toString();
                String str4 = String.valueOf(timeWindow.getValue());
                Log.i("Input: ", str1);

                sharedPref.edit().putString("favRestaurants", str1).apply();
                sharedPref.edit().putString("favRestaurantsTypes", str2).apply();
                sharedPref.edit().putString("noRestaurants", str3).apply();
                sharedPref.edit().putString("timeWindow", str4).apply();

            }
        });

        if (favRestaurantsString != "") {
            favRestaurants.setText(favRestaurantsString);
        }

        if (favRestaurantsTypesString != "") {
            favTypes.setText(favRestaurantsTypesString);
        }

        if (noRestaurantsString != "") {
            noRestaurants.setText(noRestaurantsString);
        }

        if (timeWindowString != "") {
            timeWindow.setValue(Integer.valueOf(timeWindowString));
        } else {
            timeWindow.setValue(1);
        }

    }
}
