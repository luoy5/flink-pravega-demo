package io.pravega.example.flink.demo;


import io.pravega.client.stream.Stream;
import io.pravega.connectors.flink.FlinkPravegaReader;
import io.pravega.connectors.flink.PravegaConfig;
import io.pravega.connectors.flink.serialization.PravegaDeserializationSchema;
import io.pravega.example.flink.Utils;
import io.pravega.example.flink.demo.baidu.Check;
import io.pravega.example.flink.demo.sink.FileSink;
import org.apache.commons.io.IOUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Create by Anna
 * Date 2021-08-16
 * A class for reading image from pravega
 */
public class MyReader {
    // Logger initialization
    private static final Logger LOG = LoggerFactory.getLogger(MyReader.class);

    // The application reads data from specified Pravega stream and once every 10 seconds
    // prints the distinct words and counts from the previous 10 seconds.

    // Application parameters
    //   stream - default examples/wordcount
    //   controller - default tcp://127.0.0.1:9090

    public static void main(String[] args) throws Exception {
        LOG.info("Starting MyReader...");

        // initialize the parameter utility tool in order to retrieve input parameters
        ParameterTool params = ParameterTool.fromArgs(args);
        PravegaConfig pravegaConfig = PravegaConfig
                .fromParams(params)
                .withDefaultScope(Constants.DEFAULT_SCOPE);

        // create the Pravega input stream (if necessary)
        Stream stream = Utils.createStream(
                pravegaConfig,
                params.get(Constants.STREAM_PARAM, Constants.DEFAULT_STREAM));

        // initialize the Flink execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DeserializationSchema<ImageData> deserializationSchema = new PravegaDeserializationSchema<>(
                ImageData.class, new ImageDataSerializer());

        // create the Pravega source to read a stream of ImageData
        FlinkPravegaReader<ImageData> source = FlinkPravegaReader.<ImageData>builder()
                .withPravegaConfig(pravegaConfig)
                .forStream(stream)
                .withDeserializationSchema(deserializationSchema)
                .build();

        DataStream<ImageData> dataStream = env.addSource(source).name("Pravega Stream");
        DataStream<OutCSV> bb = dataStream.map(image -> {
            OutCSV csvInfo = new OutCSV();
            InputStream inputStream = new ByteArrayInputStream(image.getData());
            String checkResult = Check.getAIResult(IOUtils.toByteArray(inputStream));
            csvInfo.setDriverId(image.getDriverId());
            csvInfo.setImageUrl(image.getUrl());
            csvInfo.setCheckResult(checkResult);
            return csvInfo;
        }).name("annaOutput");

//        bb.print();
//        bb.printToErr();

        bb.addSink(new FileSink());

        // execute within the Flink environment
        env.execute("MyReader");

        LOG.info("Ending MyReader...");
    }

}
