package io.pravega.example.flink.demo;

import lombok.Data;

import java.io.Serializable;

/**
 * Create by Anna
 * Date 2021-08-18
 * A class for output CSV
 */
@Data
public class OutCSV implements Serializable {
    private String driverId;
    private String imageUrl;
    private String checkResult;
}
