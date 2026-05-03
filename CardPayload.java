package com.shubhlab.jweencryption;

// WHAT THIS DOES: simple Java object representing the payload you want to encrypt.
public class CardPayload {
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;

    public CardPayload() {}

    public CardPayload(String cardNumber, String cardHolderName, String expiryMonth, String expiryYear) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    public String getExpiryMonth() {
        return expiryMonth;
    }
    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    public String getExpiryYear() {
        return expiryYear;
    }
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    @Override
    public String toString() {
        return "CardPayload{" +
                "cardNumber='" + cardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", expiryMonth='" + expiryMonth + '\'' +
                ", expiryYear='" + expiryYear + '\'' +
                '}';
    }
}
