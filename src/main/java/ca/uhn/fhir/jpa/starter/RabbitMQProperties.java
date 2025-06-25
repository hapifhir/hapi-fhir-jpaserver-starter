package ca.uhn.fhir.jpa.starter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
	private Queue queue;
	private Exchange exchange;
	private Binding binding;

	public static class Queue {
		private Email email;

		public static class Email {
			private String name;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}

		public Email getEmail() {
			return email;
		}

		public void setEmail(Email email) {
			this.email = email;
		}
	}

	public static class Exchange {
		private Email email;

		public static class Email {
			private String name;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}

		public Email getEmail() {
			return email;
		}

		public void setEmail(Email email) {
			this.email = email;
		}
	}

	public static class Binding {
		private Email email;

		public static class Email {
			private String name;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}

		public Email getEmail() {
			return email;
		}

		public void setEmail(Email email) {
			this.email = email;
		}
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}
}