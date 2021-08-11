package com.formation.espinoza_ssm.config;

import com.formation.espinoza_ssm.domain.PaymentEvent;
import com.formation.espinoza_ssm.domain.PaymentState;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;
import java.util.Random;

import static com.formation.espinoza_ssm.domain.PaymentEvent.*;
import static com.formation.espinoza_ssm.domain.PaymentState.*;
import static com.formation.espinoza_ssm.service.PaymentService.PAYMENT_ID_HEADER;

@Configuration
@EnableStateMachineFactory
public class PaymentStateMachineConfiguration extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .states(EnumSet.allOf(PaymentState.class))
                .initial(NEW)
                .end(AUTH)
                .end(PRE_AUTH_ERROR)
                .end(AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal().source(NEW).target(NEW).event(PRE_AUTHORIZE).action(preAuthAction()).and()
                .withExternal().source(NEW).target(PRE_AUTH).event(PRE_AUTH_APPROVED).action(authAction()).and()
                .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PRE_AUTH_DECLINED).and()
                .withExternal().source(PRE_AUTH).target(AUTH).event(AUTH_APPROVED).and()
                .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(AUTH_DECLINED).and();
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        config.withConfiguration()
                .listener(new PaymentStateMachineListener<>());
    }

    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return stateContext -> {
            System.out.println("PreAuth was called");
            Message<PaymentEvent> message;

            if (new Random().nextInt(10) < 9) {
                System.out.println("Approved");
                message = MessageBuilder
                        .withPayload(PRE_AUTH_APPROVED)
                        .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                        .build();
            } else {
                System.out.println("Declined! No Credit!!!!!!!");
                message = MessageBuilder
                        .withPayload(PRE_AUTH_DECLINED)
                        .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                        .build();
            }

            stateContext.getStateMachine().sendEvent(message);
        };
    }

    public Action<PaymentState, PaymentEvent> authAction() {
        return stateContext -> {
            System.out.println("Auth was called");
            Message<PaymentEvent> message;

            if (new Random().nextInt(10) < 9) {
                System.out.println("Auth Approved");
                message = MessageBuilder
                        .withPayload(AUTH_APPROVED)
                        .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                        .build();
            } else {
                System.out.println("Auth Declined! No Credit!!!!!!!");
                message = MessageBuilder
                        .withPayload(AUTH_DECLINED)
                        .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                        .build();
            }

            stateContext.getStateMachine().sendEvent(message);
        };
    }
}
