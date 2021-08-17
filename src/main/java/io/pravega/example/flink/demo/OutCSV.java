package io.pravega.example.flink.demo;

import lombok.Data;

import java.io.Serializable;

/**
 * A class for output CSV
 */
@Data
public class OutCSV implements Serializable {
    private String driverId;
    private String imageUrl;
    private String checkResult;
}
