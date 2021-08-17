package io.pravega.example.flink.demo.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVWriter;
import io.pravega.example.flink.demo.OutCSV;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.*;

public class FileSink extends RichSinkFunction<OutCSV> {
    private FileWriter fileWriter;
    private CSVWriter csvWriter;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        fileWriter = new FileWriter("./out.csv", true);
        csvWriter = new CSVWriter(fileWriter);
        csvWriter.writeNext(new String[]{"driverId", "url", "result"});
    }

    @Override
    public void invoke(OutCSV value, Context context) throws Exception {
        JSONObject obj = (JSONObject) JSON.toJSON(value);
        String[] line = obj.values()
                .stream()
                .map(i -> i.toString())
                .toArray(i -> new String[i]);

        System.out.println("writing:" + line);
        csvWriter.writeNext(line);
//        fileWriter.write("\n");
        csvWriter.flush();
    }
}
