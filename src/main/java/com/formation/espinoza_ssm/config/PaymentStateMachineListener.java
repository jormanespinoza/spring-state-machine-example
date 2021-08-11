package com.formation.espinoza_ssm.config;

import com.formation.espinoza_ssm.domain.PaymentEvent;
import com.formation.espinoza_ssm.domain.PaymentState;
import lombok.extern.java.Log;
import org.springframework.messaging.Message;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

@Log
@Component
public class PaymentStateMachineListener<S extends PaymentState, E extends PaymentEvent> extends StateMachineListenerAdapter<S, E> {

    @Override
    public void stateChanged(State<S, E> from, State<S, E> to) {
        log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
    }

    @Override
    public void eventNotAccepted(Message<E> event) {
        log.info(String.format("eventNotAccepted(%s)", event.getPayload()));
    }
}
