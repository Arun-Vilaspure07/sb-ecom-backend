package com.ecommerce.project.kafka.consumer;

import com.ecommerce.project.kafka.event.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationConsumer.class);

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> consumerRecord,
                                   Acknowledgment ack) {

        OrderCreatedEvent event = consumerRecord.value();

        try {
            log.info("Order event received from Kafka. OrderId={}, UserId={}, Amount={}",
                    event.getOrderId(), event.getUserId(), event.getTotalAmount());
            ack.acknowledge(); // manual commit

        } catch (Exception e) {
            log.error("Error while processing OrderCreatedEvent", e);
            // do NOT ack → Kafka will retry
        }
    }
}
