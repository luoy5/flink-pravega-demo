package io.pravega.example.flink.demo;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class MyWriter {
    public String scope;
    public String streamName;
    public URI controllerURI;
    
    public MyWriter(String scope, String streamName, URI controllerURI){
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
    
    public static void main(String[] args) throws IOException, InterruptedException {
        MyWriter writer = new MyWriter(Constants.DEFAULT_SCOPE, Constants.DEFAULT_STREAM, URI.create(Constants.DEFAULT_CONTROLLER_URI));
        String dirPath = "C:\\Flink\\flink-pravega-demo\\images";
        File file = new File(dirPath);
        File[] tempList = file.listFiles();
        for (File file1 : tempList) {
            ImageData data = new ImageData();
            data.setImageType("jpg");
            data.setDriverId("11");
            data.setUrl(file1.getAbsolutePath());
            data.setTimestamp(new Date().getTime());
            data.setData(FileUtils.readFileToByteArray(file1));
            writer.run(data.getDriverId(), data);
            Thread.sleep(100);
        }
    }
}
