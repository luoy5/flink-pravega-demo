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

/**
 * Create by Anna
 * Date 2021-08-16
 * A class for writing image to pravega
 */

public class MyWriter {
    public String scope;
    public String streamName;
    public URI controllerURI;

    public MyWriter(String scope, String streamName, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }

    public void run(String routingKey, ImageData data) {
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
        //String dirPath = "C:\\Flink\\flink-pravega-demo\\images";
        String dirPath1 = "/root/flink-pravega-demo/images/driver1";
        String dirPath2 = "/root/flink-pravega-demo/images/driver2";
        File file1 = new File(dirPath1);
        File file2 = new File(dirPath2);
        File[] tempList1 = file1.listFiles();
        File[] tempList2 = file2.listFiles();

        for (int i = 0; i < tempList1.length; i++) {
            File fileEle1 = tempList1[i];
            File fileEle2 = tempList2[i];

            ImageData data1 = new ImageData();
            data1.setImageType("jpg");
            data1.setDriverId("driver1");
            data1.setUrl(fileEle1.getAbsolutePath());
            data1.setTimestamp(new Date().getTime());
            data1.setData(FileUtils.readFileToByteArray(fileEle1));
            writer.run(data1.getDriverId(), data1);

            ImageData data2 = new ImageData();
            data2.setImageType("jpg");
            data2.setDriverId("driver2");
            data2.setUrl(fileEle2.getAbsolutePath());
            data2.setTimestamp(new Date().getTime());
            data2.setData(FileUtils.readFileToByteArray(fileEle2));
            writer.run(data2.getDriverId(), data2);
        }
    }
}
