package ca.uhn.fhir.jpa.starter;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;

@Configuration
public class RabbitMQConfig {

	@Autowired
	private RabbitMQProperties rabbitMQProperties;

	@Bean
	public Queue emailQueue(){
		return new Queue(rabbitMQProperties.getQueue().getEmail().getName());
	}

	@Bean
	public TopicExchange emailExchange(){
		return new TopicExchange(rabbitMQProperties.getExchange().getEmail().getName());
	}

	@Bean
	public Binding emailBinding(){
		return BindingBuilder.bind(emailQueue()).to(emailExchange()).with(rabbitMQProperties.getBinding().getEmail().getName());
	}

	@Bean
	public RabbitTemplate amqpTemplate(ConnectionFactory connectionFactory){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(converter());
		return rabbitTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter converter(){
		return new Jackson2JsonMessageConverter();
	}
}
