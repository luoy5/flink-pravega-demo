package io.pravega.example.flink.wordcount;

import lombok.Data;

import java.io.Serializable;

/**
 * A class for storing image
 */
@Data
public class ImageData implements Serializable {
    //driver ID
    private String driverId;
    private long timestamp;
    //png-encode image
    private byte[] data;
    private String imageType;
    private String url;
}
