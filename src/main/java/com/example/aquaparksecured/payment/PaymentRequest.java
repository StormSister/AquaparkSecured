package com.example.aquaparksecured.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class PaymentRequest {

    private Long totalPrice;
    private String paymentType;

}