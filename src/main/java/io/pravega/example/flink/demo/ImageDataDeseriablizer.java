package io.pravega.example.flink.demo;

import io.pravega.client.stream.EventRead;
import io.pravega.connectors.flink.serialization.PravegaDeserializationSchema;

/**
 * Create by Anna
 * Date 2021-08-17
 * Pravega Deserializer for ImageData
 */
public class ImageDataDeseriablizer extends PravegaDeserializationSchema<ImageData> {
    public ImageDataDeseriablizer(){
        super(ImageData.class, new ImageDataSerializer());
    }
    
    @Override
    public ImageData extractEvent(EventRead<ImageData> eventRead) {
        final ImageData event = eventRead.getEvent();
        return event;
    }
}
