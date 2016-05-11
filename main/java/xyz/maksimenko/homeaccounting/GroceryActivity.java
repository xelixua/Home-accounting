package xyz.maksimenko.homeaccounting;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class GroceryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);
    }

    public void applyButtonPressed(View view){
        Spinner shopSelect = (Spinner) findViewById(R.id.shopSpinner),
                currencySelect = (Spinner) findViewById(R.id.currencySpinner);
        String shop = (String) shopSelect.getSelectedItem();
        String currency = (String) currencySelect.getSelectedItem();
        Intent returnIntent = new Intent();
        EditText amount = (EditText) findViewById(R.id.amount_editText);
        returnIntent.putExtra("shop",shop);
        returnIntent.putExtra("amount", Integer.valueOf(amount.getText().toString()));
        returnIntent.putExtra("currency", currency);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
