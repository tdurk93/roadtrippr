package roadtrippr.roadtrippr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class SettingsActivity extends AppCompatActivity {

    MultiAutoCompleteTextView favRestaurants, favTypes, noRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    }

}
