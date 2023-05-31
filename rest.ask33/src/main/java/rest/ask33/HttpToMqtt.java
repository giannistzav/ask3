package rest.ask33;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class HttpToMqtt {

    private static final String MQTT_BROKER = "tcp://192.168.0.100:1883";
    private static final String MQTT_TOPIC = "my/topic";

    public static void main(String[] args) {
        // Start the HTTP server
        Spark.port(8080);
        Spark.post("/publish", new PublishHandler());

        // Connect to MQTT broker
        try {
            MqttClient mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.connect();
            System.out.println("Connected to MQTT broker.");

            // Subscribe to a topic if necessary
            // mqttClient.subscribe(MQTT_TOPIC);

            // Publish a sample message on startup
            String sampleMessage = "Hello!";
            mqttClient.publish(MQTT_TOPIC, new MqttMessage(sampleMessage.getBytes()));
            System.out.println("Published message: " + sampleMessage);

            // Keep the application running
            System.in.read();

            // Disconnect from MQTT broker
            mqttClient.disconnect();
            System.out.println("Disconnected from MQTT broker.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PublishHandler implements Route {
        @Override
        public Object handle(Request request, Response response) {
            try {
                // Extract the message from the POST request
                String message = request.body();
                System.out.println("Received HTTP POST: " + message);

                // Publish the message using MQTT
                MqttClient mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
                mqttClient.connect();
                mqttClient.publish(MQTT_TOPIC, new MqttMessage(message.getBytes()));
                mqttClient.disconnect();
                System.out.println("Published message via MQTT: " + message);

                return "Message published via MQTT: " + message;
            } catch (MqttException e) {
                e.printStackTrace();
                response.status(500);
                return "Error publishing message: " + e.getMessage();
            }
        }
    }
}