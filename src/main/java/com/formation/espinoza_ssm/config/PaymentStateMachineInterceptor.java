package com.formation.espinoza_ssm.config;

import com.formation.espinoza_ssm.domain.Payment;
import com.formation.espinoza_ssm.domain.PaymentEvent;
import com.formation.espinoza_ssm.domain.PaymentState;
import com.formation.espinoza_ssm.repository.PaymentRepository;
import com.formation.espinoza_ssm.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateMachineInterceptor<S extends PaymentState, E extends PaymentEvent> extends StateMachineInterceptorAdapter<S, E> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<S, E> state,
                               Message<E> message,
                               Transition<S, E> transition,
                               StateMachine<S, E> stateMachine,
                               StateMachine<S, E> rootStateMachine) {
        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(PaymentService.PAYMENT_ID_HEADER, -1L)))
                .ifPresent(paymentId -> {
                    Payment payment = paymentRepository.getById(paymentId);
                    payment.setState(state.getId());
                    paymentRepository.save(payment);
                });
    }
}
