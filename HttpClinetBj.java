package com.example.pats;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This is Description
 *
 * @author liyadong
 * @date 2020/08/06
 */
public class HttpClinetBj {

    public static void main(String[] args) throws InterruptedException {

        JSONObject param = new JSONObject();

        int unitId = 210;

        int lessionId = 4653;

        for (int i = 0; i < 6; i++) {
            System.out.println("开始刷课程lessionId :" + lessionId);

            dealLession(param, unitId ,lessionId);
            lessionId+=1;
        }
    }

    /**
     * 刷课
     */
    private static void dealLession(JSONObject param,int unitId, int lessionId) throws InterruptedException {
        //查询时长
        String timeResult = sendPost("https://www.bjjnts.cn/lessonStudy/"+ unitId +"/" + lessionId,null);

        JSONObject jsonObject = JSONObject.parseObject(timeResult);

        JSONObject jsonObject1 = JSON.parseObject(jsonObject.get("data").toString());

        Integer duration = Integer.valueOf(jsonObject1.get("duration").toString());

        Integer learnDuration = Integer.valueOf(jsonObject1.get("learnDuration").toString());

        if (learnDuration == 0){
            learnDuration = 70;
        }else {
            if (!duration.equals(learnDuration)){
                learnDuration+=1;
            }else {
                System.out.println("该课程已学完，课程id : " + lessionId);
                return;
            }
        }

        for (int i = learnDuration; i < duration; i+=70) {


            param.put("learnTime",i);
            param.put("push_event","update");

            System.out.println("本次更新课程参数为 ：" + param.toJSONString());

            String s = sendPost("https://www.bjjnts.cn/addstudentTaskVer2/"+unitId+"/" + lessionId, param);

            JSONObject result = JSONObject.parseObject(s);

            System.out.println("本次更新结果为 ：" + JSON.toJSONString(result));

            if (i + 70 >= duration){

                //结束该课程
//                param.put("push_event","ended");
//                param.put("learnTime",duration);
//
//                System.out.println("尝试结束课程入参 ：" + param.toJSONString() + "------课程lessionId :" + lessionId);
//                String end = sendPost("https://www.bjjnts.cn/addstudentTaskVer2/761/" + lessionId, param);
//                JSONObject endResult = JSONObject.parseObject(end);
//                System.out.println("尝试结束课程返回结果 ：" + endResult.toJSONString());
                JSONObject endResult = HttpClinetEndBj.endLession(unitId, lessionId, duration);


                Integer endCode = Integer.valueOf(endResult.get("code").toString());

                if (endCode == 100){
                    System.out.println("结束课程异常信息 ：" + endResult.get("msg"));
                    break;
                }
                if (endCode == 200){
                    System.out.println("该课程已学完，课程lessionId :" + lessionId);
                    break;
                }
            }

        }
    }

    public static String sendPost(String url, JSONObject param) {
        OutputStream out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Cookie","GUIDE_MAP:=1596420128; Hm_lvt_83efb6da7f0d1882680d3ee8ad0d78f0=1596420131; PHPSESSID=s3gjj47ct4qpl10t4gqn4b95s4; acw_tc=2760827815967186546027695e213219c5210c5cbacb8f851427620d2126cb; Hm_lpvt_83efb6da7f0d1882680d3ee8ad0d78f0=1596718655");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = conn.getOutputStream();
            if (param != null){
                // 发送请求参数
                StringBuffer params = new StringBuffer();

                params.append("learnTime").append("=").append(param.get("learnTime")).append("&").append("push_event").append("=").append(param.get("push_event"));

                out.write(params.toString().getBytes());
            }else {
                out.write("".getBytes());
            }
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null){
                    out.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
