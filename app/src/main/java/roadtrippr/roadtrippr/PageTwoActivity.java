package roadtrippr.roadtrippr;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

public class PageTwoActivity extends AppCompatActivity {

    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;

    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_two);

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

        if(favRestaurantsString != "") {

            favRestaurants.setText(favRestaurantsString);

        }

        if(favRestaurantsTypesString != "") {

            favTypes.setText(favRestaurantsTypesString);

        }

        if(noRestaurantsString != "") {

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

            }
        });

    }
}
