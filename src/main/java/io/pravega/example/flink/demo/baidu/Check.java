package io.pravega.example.flink.demo.baidu;

import java.net.URLEncoder;

public class Check {
    public static String aiUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v1/driver_behavior";
    
    public static String getAIResult(String filePath) throws Exception {
        byte[] imageData = FileUtil.readFileByBytes(filePath);
        return getAIResult(imageData);
    }
    
    public static String getAIResult(byte[] imageData) throws Exception {
        String imageStr = Base64Util.encode(imageData);
        String imageParam = URLEncoder.encode(imageStr, "UTF-8");

        String param =  "image=" + imageParam;
        String accessToken = AuthService.getAuth();
        return HttpUtil.post(aiUrl, accessToken, param);
    }

    public static void main(String[] args) throws Exception {
        String res = Check.getAIResult("C:\\Flink\\flink-pravega-demo\\images\\4.JPG");
        System.out.println(res);
    }
}
