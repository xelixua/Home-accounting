package xyz.maksimenko.homeaccounting.backend;

import xyz.maksimenko.homeaccounting.FinancialOperations.FinancialOperation;
import xyz.maksimenko.homeaccounting.MainActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by smaksimenko on 4/5/2016.
 */
public class GoogleSpreadsheets implements BackendProcessor {
    private GoogleAccountCredential mCredential = null;
    private MainActivity mainActivity;

    public GoogleSpreadsheets(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setCreadentials(GoogleAccountCredential mCredential){
        this.mCredential = mCredential;
    }

    @Override
    public void store(FinancialOperation operation) {
        new MakeRequestTask(mCredential, operation).execute();
    }

    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }

    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;
        private FinancialOperation operation = null;

        public MakeRequestTask(GoogleAccountCredential credential, FinancialOperation operation) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, setHttpTimeout(credential))
                    .setApplicationName("Home accounting")
                    .build();

            this.operation = operation;
        }

        /**
         * Background task to call Google Apps Script Execution API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi(operation);
            } catch (Exception e) {
                mLastError = e;
                e.printStackTrace();
                cancel(true);
                return null;
            }
        }

    /**
     * Call the API to run an Apps Script function that returns a list
     * of folders within the user's root directory on Drive.
     *
     * @return list of String folder names and their IDs
     * @throws IOException
     */
        private String getDataFromApi(FinancialOperation operation)
                throws IOException, GoogleAuthException {
            String rate = null;
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
            String scriptId = "My4bg1hL-bdajBcao7RtPCiUUOHH9NW3O";

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest().setFunction("ADDOPERATION");

            List<Object> parameters = new ArrayList<Object>();
            DateFormat df = new SimpleDateFormat("d.MM.yy");
            Date date = new Date(operation.getDate());
            String s_date = df.format(date);
            parameters.add(s_date);

            parameters.add(String.valueOf(operation.type));

            parameters.add(operation.getName());
            parameters.add(String.valueOf(operation.getAmount()));
            parameters.add(operation.getStringCurrency());

            request.setParameters(parameters);
            for(int i = 0; i < parameters.size(); i++){
                Log.i("Home accounting", parameters.get(i).toString());
            }
            // Make the request.
            Operation op =
                    mService.scripts().run(scriptId, request).execute();

            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }

            Log.i("Home accounting", String.valueOf(op.getResponse().entrySet().size()));
            Iterator itty = op.getResponse().entrySet().iterator();
            while(itty.hasNext()){
                Map.Entry<String, Object> ent = (Map.Entry<String, Object> ) itty.next();
                Log.i("Home accounting", ent.getKey() + ": " + ent.getValue());
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                // The result provided by the API needs to be cast into
                // the correct type, based upon what types the Apps Script
                // function returns. Here, the function returns an Apps
                // Script Object with String keys and values, so must be
                // cast into a Java Map (folderSet).
                rate =
                        (String) (op.getResponse().get("result"));


            }

            return rate;
        }

    /**
     * Interpret an error response returned by the API and return a String
     * summary.
     *
     * @param op the Operation returning an error response
     * @return summary of error response, or null if Operation returned no
     *     error
     */
        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>)detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            return sb.toString();
        }


       @Override
        protected void onPreExecute() {
            //mainActivity.mOutputText.setText("");
            mainActivity.mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mainActivity.mProgress.hide();
            if (output == null ) {
                Log.i("Home accounting", "No results returned");
               // mainActivity.mOutputText.setText("No results returned.");
            } else {
               // mainActivity.mOutputText.setText(output);
                Log.i("Home accounting", output);
            }
        }

        @Override
        protected void onCancelled() {
            mainActivity.mProgress.hide();
            Log.i("Home accounting", "Request was cancelled");
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    mainActivity.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    mainActivity.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            mainActivity.reqAuth);
                } else {
                }
            } else {
            }
        }
    }
}
