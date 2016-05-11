package xyz.maksimenko.homeaccounting.backend;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import xyz.maksimenko.homeaccounting.FinancialOperations.FinancialOperation;
import xyz.maksimenko.homeaccounting.FinancialOperations.Payments.Payment;

/**
 * Created by smaksimenko on 4/5/2016.
 */
public interface BackendProcessor {
    public void store(FinancialOperation operation);
}
