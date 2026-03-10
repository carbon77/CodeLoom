package com.codeloom.backend.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaConfig(
    @Value("\${codeloom.kafka.submission-topic}")
    private val submissionsTopic: String,
) {
    @Bean
    fun stringProducerFactory(properties: KafkaProperties): DefaultKafkaProducerFactory<String, String> {
        val producerProperties = properties.buildProducerProperties(null).toMutableMap()
        producerProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return DefaultKafkaProducerFactory(producerProperties)
    }

    @Bean
    fun stringKafkaTemplate(stringProducerFactory: DefaultKafkaProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(stringProducerFactory)
    }

    @Bean
    fun submissionTopic(): NewTopic {
        return TopicBuilder.name(submissionsTopic).build()
    }

    @Bean
    fun stringConsumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, String> {
        val props = kafkaProperties.buildConsumerProperties(null).toMutableMap()
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun stringListenerFactory(stringConsumerFactory: ConsumerFactory<String, String>): KafkaListenerContainerFactory<*> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = stringConsumerFactory
        factory.isBatchListener = false
        return factory
    }
}