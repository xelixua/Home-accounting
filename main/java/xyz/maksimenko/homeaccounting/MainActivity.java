package xyz.maksimenko.homeaccounting;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;

import xyz.maksimenko.homeaccounting.FinancialOperations.FinancialOperation;
import xyz.maksimenko.homeaccounting.FinancialOperations.Payments.DEATPayment;
import xyz.maksimenko.homeaccounting.FinancialOperations.Payments.GroceryPayment;
import xyz.maksimenko.homeaccounting.FinancialOperations.Payments.TransportPayment;
import xyz.maksimenko.homeaccounting.backend.GoogleSpreadsheets;

public class MainActivity extends Activity {
    GoogleAccountCredential mCredential;
    public ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_SHOP_PROCESS = 666;
    static final int REQUEST_TRANSPORT_PROCESS = 667;
    private static final String PREF_ACCOUNT_NAME = "xelixua@gmail.com";
    private static final String[] SCOPES = { "https://www.googleapis.com/auth/spreadsheets" };
    private GoogleSpreadsheets backendP = new GoogleSpreadsheets(this);
    public int reqAuth = MainActivity.REQUEST_AUTHORIZATION;
    private Cursor cursor;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Обновление данных на сервере ...");
        setContentView(R.layout.activity_main);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        backendP.setCreadentials(mCredential);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            //refreshResults();
        } else {
            //mOutputText.setText("Google Play Services required: " +
                    //"after installing, close and relaunch this app.");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    //mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
            case REQUEST_SHOP_PROCESS:
                if(resultCode == RESULT_OK) {
                    String shop = data.getStringExtra("shop");
                    short amount = (short) data.getIntExtra("amount", 0);
                    String currency = data.getStringExtra("currency");
                    GroceryPayment shopPayment = new GroceryPayment();
                    shopPayment.setName(shop);
                    shopPayment.setAmount(amount);
                    if(currency.equals("руб")) {
                        shopPayment.setCurrency(FinancialOperation.Currency.RUB);
                    } else {
                        shopPayment.setCurrency(FinancialOperation.Currency.UAH);
                    }
                    prepareToStore(shopPayment);
                }
                break;
            case REQUEST_TRANSPORT_PROCESS:
                if(resultCode == RESULT_OK){
                    int amount = data.getIntExtra("amount", 0);
                    TransportPayment transport = new TransportPayment();
                    transport.setAmount((short) amount);
                    prepareToStore(transport);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Apps Script Execution API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                //new MakeRequestTask(mCredential).execute();
                //backendP.mrt(mCredential).execute();
            } else {
                //mOutputText.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void transportButtonClicked(View view) {
        Intent intent = new Intent();
        // определение класса запускаемой активности
        intent.setClass(this, TransportActivity.class);
        startActivityForResult(intent, REQUEST_TRANSPORT_PROCESS);
    }

    public void groceryButtonClicked(View view) {
        Intent intent = new Intent();
        // определение класса запускаемой активности
        intent.setClass(this, GroceryActivity.class);
        startActivityForResult(intent, REQUEST_SHOP_PROCESS);
    }

    private void prepareToStore(FinancialOperation operation){
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                backendP.store(operation);
            } else {
                //mOutputText.setText("No network connection available.");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transport_deat:
                DEATPayment avtobus = new DEATPayment();
                prepareToStore(avtobus);
                return true;
            case R.id.action_transport_marshrutka_cheap:
                TransportPayment goodLittleBus = new TransportPayment();
                goodLittleBus.setAmount((short) 6);
                prepareToStore(goodLittleBus);
                return true;
            case R.id.action_transport_marshrutka:
                TransportPayment badLittleBus = new TransportPayment();
                badLittleBus.setAmount((short) 7);
                prepareToStore(badLittleBus);
                return true;
            case R.id.action_grocery:
                Intent intent = new Intent();
                // определение класса запускаемой активности
                intent.setClass(this, GroceryActivity.class);
                startActivityForResult(intent, REQUEST_SHOP_PROCESS);
                return true;
            case R.id.action_settings:
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}