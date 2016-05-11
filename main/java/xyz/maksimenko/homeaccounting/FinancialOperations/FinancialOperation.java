package xyz.maksimenko.homeaccounting.FinancialOperations;

import java.util.Calendar;

/**
 * Created by smaksimenko on 4/5/2016.
 */
public class FinancialOperation {
    public byte type;
    public enum Currency {
        UAH, RUB
    }
    protected Long date;
    protected String name;
    protected short amount;
    protected Currency currency;

    public FinancialOperation () {
        this.date = Calendar.getInstance().getTimeInMillis();

    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public short getAmount() {
        return amount;
    }

    public void setAmount(short amount) {
        this.amount = amount;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getStringCurrency() {
        if(currency == Currency.RUB){
            return "руб";
        }

        return "грн";
    }
}
