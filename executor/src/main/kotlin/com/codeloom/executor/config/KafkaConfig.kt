package com.codeloom.executor.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
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
    val submissionTopic: String,
    @Value("\${codeloom.kafka.submission-changed-topic}")
    val submissionChangedTopic: String,
) {
    @Bean
    fun stringProducerFactory(): DefaultKafkaProducerFactory<String, String> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            )
        )

    @Bean
    fun stringKafkaTemplate(
        stringProducerFactory: DefaultKafkaProducerFactory<String, String>,
    ): KafkaTemplate<String, String> =
        KafkaTemplate(stringProducerFactory)

    @Bean
    fun submissionTopic(): NewTopic = TopicBuilder.name(submissionChangedTopic).build()

    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            )
        )

    @Bean
    fun stringListenerFactory(): KafkaListenerContainerFactory<*> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(stringConsumerFactory())
        return factory
    }
}