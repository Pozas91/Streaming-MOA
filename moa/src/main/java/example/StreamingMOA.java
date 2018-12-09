package example;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Prediction;
import moa.classifiers.Classifier;
import moa.classifiers.rules.AMRulesRegressor;
import moa.streams.ArffFileStream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class StreamingMOA {
    private final String topic;
    private final Properties props;
    private Classifier learner;
    private ArffFileStream offlineStream;
    private static final String DATA_PATH = "./data";

    private StreamingMOA(String brokers, String username, String password, String topic) {
        this.topic = topic;

        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);

        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();
        props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", username + "-consumer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", deserializer);
        props.put("value.deserializer", deserializer);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("sasl.jaas.config", jaasCfg);

        this.learner = new AMRulesRegressor();
        this.offlineStream = new ArffFileStream(DATA_PATH + "/offline.arff", -1);
    }

    private void OfflineTraining() {

        // Show action
        System.out.println("Offline training...");

        // Prepare stream to use
        this.offlineStream.prepareForUse();

        // Prepare learner
        this.learner.setModelContext(this.offlineStream.getHeader());
        this.learner.prepareForUse();

        // Train with all data in first arff file.
        while (this.offlineStream.hasMoreInstances()) {
            // Get instance
            Instance instance = this.offlineStream.nextInstance().getData();
            // Train
            this.learner.trainOnInstance(instance);
        }
    }

    private void consumeDataFromKafka() throws IOException {
        String json;

        org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {

                // Get json
                json = record.value();

                // Parsing data from kafka
                TypeToken<Set<Parking>> token = new TypeToken<Set<Parking>>() {};
                GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
                Set<Parking> data = gsonBuilder.create().fromJson(json, token.getType());

                // Train learner.
                train(data);
            }

            // Check if user wants to predict.
            predictIfExistsFile();
        }
    }

    private void train(Set<Parking> data) throws IOException {

        // Show action
        System.out.println("Training learner...");

        // Build arff file.
        ConvertToArff convertToArff = new ConvertToArff(DATA_PATH + "/new");
        String path = convertToArff.convert(data);

        // Create stream with new data.
        ArffFileStream stream = new ArffFileStream(path, -1);
        stream.prepareForUse();

        // Prepare learner
        this.learner.setModelContext(stream.getHeader());
        this.learner.prepareForUse();

        // Train with new data.
        while (stream.hasMoreInstances()) {
            Instance instance = stream.nextInstance().getData();
            this.learner.trainOnInstance(instance);
        }

        // Delete previous file
        (new File(path)).delete();
    }

    private void predictIfExistsFile() {
        // Prepare to check test file.
        File file = new File(DATA_PATH + "/predict.arff");

        // If we have data to test, test!
        if (file.exists() && !file.isDirectory()) {

            // Arff file to stream.
            ArffFileStream testStream = new ArffFileStream(file.getPath(), -1);
            testStream.prepareForUse();

            while (testStream.hasMoreInstances()) {
                // Get instance
                Instance testInstance = testStream.nextInstance().getData();

                // Make a prediction
                Prediction prediction = this.learner.getPredictionForInstance(testInstance);

                // Show result
                System.out.println("For instance: " + testInstance);
                System.out.println("Prediction is: " + prediction);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Load settings
        String brokers = KafkaSettings.CLOUDKARAFKA_BROKERS;
        String username = KafkaSettings.CLOUDKARAFKA_USERNAME;
        String password = KafkaSettings.CLOUDKARAFKA_PASSWORD;
        String topic = KafkaSettings.CLOUDKARAFKA_TOPICS;

        // Instance simulate MOA streaming
        StreamingMOA streamingMOA = new StreamingMOA(brokers, username, password, topic);

        // Do initial training
        streamingMOA.OfflineTraining();

        // Consume data from Kafka
        streamingMOA.consumeDataFromKafka();
    }
}