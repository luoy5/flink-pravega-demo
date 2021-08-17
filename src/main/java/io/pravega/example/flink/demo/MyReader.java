package io.pravega.example.flink.demo;

import com.alibaba.fastjson.JSON;
import io.pravega.client.stream.Stream;
import io.pravega.connectors.flink.FlinkPravegaReader;
import io.pravega.connectors.flink.PravegaConfig;
import io.pravega.connectors.flink.serialization.PravegaDeserializationSchema;
import io.pravega.example.flink.Utils;
import io.pravega.example.flink.demo.baidu.Check;
import org.apache.commons.io.IOUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
                .withDefaultScope("hello");

        // create the Pravega input stream (if necessary)
        Stream stream = Utils.createStream(
                pravegaConfig,
                params.get(Constants.STREAM_PARAM, "anna2"));

        // initialize the Flink execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        DeserializationSchema<ImageData> deserializationSchema = new PravegaDeserializationSchema<>(
                ImageData.class, new ImageDataSerializer());
        
        
        // create the Pravega source to read a stream of text
        FlinkPravegaReader<ImageData> source = FlinkPravegaReader.<ImageData>builder()
                .withPravegaConfig(pravegaConfig)
                .forStream(stream)
                .withDeserializationSchema(deserializationSchema)
                .build();
        
        // count each word over a 10 second time period
        DataStream<ImageData> dataStream = env.addSource(source).name("Pravega Stream");
                
        DataStream<String> bb = dataStream.map(image -> {
            InputStream inputStream = new ByteArrayInputStream(image.getData());
            String checkResult = Check.getAIResult(IOUtils.toByteArray(inputStream));
            System.out.println("checkResult = " + checkResult);
            System.out.println("image = " + image.getUrl());
            BufferedImage bufferedImage = ImageIO.read(inputStream);
//            String msg = String.format("annnn: %s; dd : %s", image.getImageType(), bufferedImage.toString());
//            System.out.println(msg);
            return checkResult;
        }).name("annaOutput");
        
        
        // create an output sink to print to stdout for verification
        bb.print();
        bb.printToErr();

        // execute within the Flink environment
        env.execute("MyReader");

        LOG.info("Ending MyReader...");
    }
    
}
