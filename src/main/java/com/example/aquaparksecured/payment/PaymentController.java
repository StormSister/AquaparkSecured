package com.example.aquaparksecured.payment;



import com.stripe.Stripe;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import com.stripe.model.checkout.Session;


import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody PaymentRequest paymentRequest) throws Exception {
        System.out.println(paymentRequest);
        Stripe.apiKey = stripeApiKey;
        System.out.println(stripeApiKey);

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/cancel");

        paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setUnitAmount(paymentRequest.getTotalPrice())
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Ticket Purchase")
                                                        .build())
                                        .build())
                        .build());

        Session session = Session.create(paramsBuilder.build());
        System.out.println("Session ID: " + session.getId());

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<String> confirmPayment(@RequestBody PaymentConfirmationRequest request) {
        System.out.println(request);
        try {
            Stripe.apiKey = stripeApiKey;
            Session session = Session.retrieve(request.getSessionId());
            System.out.println("Payment status: " + session.getPaymentStatus());
            if ("paid".equals(session.getPaymentStatus())) {
                return ResponseEntity.ok("Payment confirmed");
            } else {
                return ResponseEntity.badRequest().body("Payment not completed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error confirming payment");
        }
    }
}