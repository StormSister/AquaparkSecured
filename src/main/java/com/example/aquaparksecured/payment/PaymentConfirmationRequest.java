package com.example.aquaparksecured.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PaymentConfirmationRequest {
    private String sessionId;
}