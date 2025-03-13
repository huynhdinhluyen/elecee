package com.example.electrical_preorder_system_backend.dto.request.payment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentPayloadData {
    long orderCode;
    long amount;
    String description;
    String accountNumber;
    String reference;
    String transactionDateTime;
    String currency;
    String paymentLinkId;
    String code;
    String desc;
    String counterAccountBankId;
    String counterAccountBankName;
    String counterAccountName;
    String counterAccountNumber;
    String virtualAccountName;
    String virtualAccountNumber;
}