package com.formation.espinoza_ssm.service;

import com.formation.espinoza_ssm.config.PaymentStateMachineInterceptor;
import com.formation.espinoza_ssm.domain.Payment;
import com.formation.espinoza_ssm.domain.PaymentEvent;
import com.formation.espinoza_ssm.domain.PaymentState;
import com.formation.espinoza_ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService<S extends PaymentState, E extends PaymentEvent> {

    public static final String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<S, E> paymentStateMachineFactory;
    private final PaymentStateMachineInterceptor<S, E> paymentStateMachineInterceptor;

    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    public StateMachine<S, E> preAuth(Payment payment) {
        StateMachine<S, E> sm = build(payment);
        sendEvent(payment.getId(), sm, PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Transactional
    public StateMachine<S, E> authorizePayment(Payment payment) {
        StateMachine<S, E> sm = build(payment);
        sendEvent(payment.getId(), sm, PaymentEvent.AUTH_APPROVED);
        return sm;
    }

    @Transactional
    public StateMachine<S, E> declineAuth(Payment payment) {
        StateMachine<S, E> sm = build(payment);
        sendEvent(payment.getId(), sm, PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<S, E> sm, PaymentEvent event) {
        Message message = MessageBuilder
                .withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();
        sm.sendEvent(message);
    }

    private StateMachine<S, E> build(Payment payment) {
        StateMachine<S, E> sm = paymentStateMachineFactory.getStateMachine(Long.toString(payment.getId()));
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateMachineInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext(payment.getState(), null, null, null));
                });
        sm.start();
        return sm;
    }
}
