package com.formation.espinoza_ssm.service;

import com.formation.espinoza_ssm.domain.Payment;
import com.formation.espinoza_ssm.domain.PaymentEvent;
import com.formation.espinoza_ssm.domain.PaymentState;
import com.formation.espinoza_ssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.formation.espinoza_ssm.domain.PaymentState.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest<S extends PaymentState, E extends PaymentEvent> {

    @Autowired
    PaymentService<S, E> paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment currentPayment = paymentService.newPayment(payment);

        StateMachine<S, E> sm = paymentService.preAuth(currentPayment);

        assertTrue(Arrays.asList(PRE_AUTH, PRE_AUTH_ERROR).contains(currentPayment.getState()));
    }

    @Transactional
    @Test
    void preAuth_fails_due_to_not_corresponding_event() {
        Payment currentPayment = paymentService.newPayment(payment);

        StateMachine<S, E> sm = paymentService.declineAuth(currentPayment);

        assertEquals(PaymentState.NEW, sm.getState().getId());
        assertNotEquals(PRE_AUTH, sm.getState().getId());
        assertNotEquals(PRE_AUTH, currentPayment.getState());
    }

    @Transactional
    @Test
    void auth() {
        Payment currentPayment = paymentService.newPayment(payment);

        StateMachine<S, E> sm = paymentService.preAuth(currentPayment);

        assertTrue(Arrays.asList(PRE_AUTH, PRE_AUTH_ERROR).contains(currentPayment.getState()));

        if (currentPayment.getState() == PRE_AUTH) {
            sm = paymentService.authorizePayment(currentPayment);

            assertTrue(Arrays.asList(AUTH, AUTH_ERROR).contains(currentPayment.getState()));
        }
    }
}