package io.pravega.example.flink.wordcount;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.flink.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class ImageWriter {
    public String scope;
    public String streamName;
    public URI controllerURI;
    
    public ImageWriter(String scope, String streamName, URI controllerURI){
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }
    
    public void run(String routingKey, ImageData data){
        StreamManager streamManager = StreamManager.create(controllerURI);
        final boolean scopeIsNew = streamManager.createScope(scope);
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(1))
                .build();

        final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);
        
        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                ClientConfig.builder().controllerURI(controllerURI).build());
             EventStreamWriter<ImageData> writer = clientFactory.createEventWriter(streamName,
                     new ImageDataSerializer(),
                     EventWriterConfig.builder().build())) {

            System.out.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n",
                    data.toString(), routingKey, scope, streamName);
            final CompletableFuture writeFuture = writer.writeEvent(routingKey, data);
        }
    }

    public void run(String routingKey, String data){
        StreamManager streamManager = StreamManager.create(controllerURI);
        final boolean scopeIsNew = streamManager.createScope(scope);
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(1))
                .build();

        final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);

        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                ClientConfig.builder().controllerURI(controllerURI).build());
             EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
                     new JavaSerializer<>(),
                     EventWriterConfig.builder().build())) {

            System.out.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n",
                    data, routingKey, scope, streamName);
            writer.writeEvent(routingKey, data);
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        ImageWriter writer = new ImageWriter("hello", "anna2", URI.create(Constants.DEFAULT_CONTROLLER_URI));
        String dirPath = "C:\\Flink\\flink-pravega-demo\\images";
        File file = new File(dirPath);
        File[] tempList = file.listFiles();
        for (File file1 : tempList) {
            ImageData data = new ImageData();
            data.setImageType("jpg");
            data.setDriverId("11");
            data.setTimestamp(new Date().getTime());
            data.setData(FileUtils.readFileToByteArray(file1));
            writer.run(data.getDriverId(), data);
            Thread.sleep(100);
        }
//        
//        String [] aa = {"a","b","c"};
//        for (String a : aa){
//            writer.run("11", a);
//            Thread.sleep(3000);
//        }
    }
}
