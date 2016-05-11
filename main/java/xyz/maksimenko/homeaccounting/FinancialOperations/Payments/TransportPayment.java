package xyz.maksimenko.homeaccounting.FinancialOperations.Payments;

/**
 * Created by smaksimenko on 3/25/2016.
 */
public class TransportPayment extends Payment{
    public TransportPayment() {
        super();
        this.name = "Транспорт";
        this.currency = Currency.RUB;
    }

}
