package roadtrippr.roadtrippr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

public class PageTwoActivity extends AppCompatActivity {

    Button submitButton;
    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_two);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide soft keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        submitButton = (Button) findViewById(R.id.submitButton);

        final SharedPreferences sharedPreferences = this.getSharedPreferences("roadtrippr.roadtrippr", Context.MODE_PRIVATE);
        String favRestaurantsString = sharedPreferences.getString("favRestaurants", "");
        String favRestaurantsTypesString = sharedPreferences.getString("favRestaurantsTypes", "");
        String noRestaurantsString = sharedPreferences.getString("noRestaurants", "");

        favRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView);
        favTypes = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView2);
        noRestaurants = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView3);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.restaurants, android.R.layout.simple_list_item_1);
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this, R.array.restaurants_types, android.R.layout.simple_list_item_1);

        favRestaurants.setAdapter(adapter);
        favRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        favTypes.setAdapter(adapter2);
        favTypes.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        noRestaurants.setAdapter(adapter);
        noRestaurants.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        if (favRestaurantsString != "") {
            favRestaurants.setText(favRestaurantsString);
        }

        if (favRestaurantsTypesString != "") {
            favTypes.setText(favRestaurantsTypesString);
        }

        if (noRestaurantsString != "") {
            noRestaurants.setText(noRestaurantsString);
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Implement Next Activity & Calculate Trip

                String str1 = favRestaurants.getText().toString();
                String str2 = favTypes.getText().toString();
                String str3 = noRestaurants.getText().toString();

                Log.i("Favorite Restaurants: ", str1);
                Log.i("Favorite Types: ", str2);
                Log.i("No Restaurants: ", str3);

                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=Niagara+Falls+NY");

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);

            }
        });

    }
}
