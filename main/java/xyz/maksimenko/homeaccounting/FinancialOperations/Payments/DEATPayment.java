package xyz.maksimenko.homeaccounting.FinancialOperations.Payments;

/**
 * Created by smaksimenko on 3/25/2016.
 */
public class DEATPayment extends TransportPayment{
    public DEATPayment(){
        this.amount = 3;
        this.currency = Currency.RUB;
    }
}
