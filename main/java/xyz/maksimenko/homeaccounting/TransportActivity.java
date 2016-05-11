package xyz.maksimenko.homeaccounting;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import xyz.maksimenko.homeaccounting.FinancialOperations.Payments.DEATPayment;

public class TransportActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport);
        TextView personNumberDescr = (TextView) findViewById(R.id.textView4);
        Spinner personNumberSpinner = (Spinner) findViewById(R.id.personNumberSpinner);
        Spinner currencySpinner = (Spinner) findViewById(R.id.currencySpinner);
        TextView sumDescr = (TextView) findViewById(R.id.textView3);
        EditText sumEditText = (EditText) findViewById(R.id.sumEditText);
        Button processPayment = (Button) findViewById(R.id.sendPaymentButton);
        personNumberDescr.setVisibility(View.INVISIBLE);
        personNumberSpinner.setVisibility(View.INVISIBLE);
        personNumberSpinner.setSelection(0);
        sumDescr.setVisibility(View.INVISIBLE);
        sumEditText.setVisibility(View.INVISIBLE);
        currencySpinner.setVisibility(View.INVISIBLE);
        processPayment.setEnabled(false);

        ((RadioGroup) findViewById(R.id.chooseTransportRadioGroup)).check(0);
    }

    public void onRadioButtonClicked(View view) {
        Button processPayment = (Button) findViewById(R.id.sendPaymentButton);
        processPayment.setEnabled(true);
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        TextView personNumberDescr = (TextView) findViewById(R.id.textView4);
        Spinner personNumberSpinner = (Spinner) findViewById(R.id.personNumberSpinner);
        Spinner currencySpinner = (Spinner) findViewById(R.id.currencySpinner);
        TextView sumDescr = (TextView) findViewById(R.id.textView3);
        EditText sumEditText = (EditText) findViewById(R.id.sumEditText);

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.anotherTransportRadioButton:
                if (checked)
                    personNumberDescr.setVisibility(View.INVISIBLE);
                    personNumberSpinner.setVisibility(View.INVISIBLE);
                    sumDescr.setVisibility(View.VISIBLE);
                    sumEditText.setVisibility(View.VISIBLE);
                    currencySpinner.setVisibility(View.VISIBLE);
                    break;
            default:
                    personNumberDescr.setVisibility(View.VISIBLE);
                    personNumberSpinner.setVisibility(View.VISIBLE);
                    sumDescr.setVisibility(View.INVISIBLE);
                    sumEditText.setVisibility(View.INVISIBLE);
                    currencySpinner.setVisibility(View.INVISIBLE);
                    break;
        }
    }

    public void sendPaymentButtonPressed(View view) {
        int costPerTicket = 0, personNumber = 0;
        String currency = "руб";
        Spinner personNumberSpinner = (Spinner) findViewById(R.id.personNumberSpinner);
        Spinner currencySpinner = (Spinner) findViewById(R.id.currencySpinner);

        if(((RadioButton) findViewById(R.id.deatRadioButton)).isChecked()){
            costPerTicket = new DEATPayment().getAmount();
            personNumber = Integer.valueOf((String) personNumberSpinner.getSelectedItem());
        }

        if(((RadioButton) findViewById(R.id.cheapLittleBusRadioButton)).isChecked()){
            costPerTicket = 6;
            personNumber = Integer.valueOf((String) personNumberSpinner.getSelectedItem());
        }

        if(((RadioButton) findViewById(R.id.littleBusRadioButton)).isChecked()){
            costPerTicket = 7;
            personNumber = Integer.valueOf((String) personNumberSpinner.getSelectedItem());
        }

        if(((RadioButton) findViewById(R.id.anotherTransportRadioButton)).isChecked()){
            costPerTicket = Integer.valueOf(((EditText) findViewById(R.id.sumEditText)).getText().toString());
            personNumber = 1;
        }

        currency = (String) currencySpinner.getSelectedItem();
        Intent returnIntent = new Intent();
        returnIntent.setAction("transport");
        returnIntent.putExtra("amount", costPerTicket * personNumber);
        returnIntent.putExtra("currency", currency);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
