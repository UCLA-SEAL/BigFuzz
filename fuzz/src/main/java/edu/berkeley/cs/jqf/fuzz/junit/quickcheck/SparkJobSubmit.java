package edu.berkeley.cs.jqf.fuzz.junit.quickcheck;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SparkJobSubmit {

    // base path
//    private static final String basePath = "/Users/zhuhaichao/Desktop/";
    private static final String basePath = "/home/qzhang/Downloads/";

    // 记录Shell执行状况的日志文件的位置(绝对路径)
    private static final String executeShellLogFile = basePath
            + "executeShell.log";

    // 发送文件到Kondor系统的Shell的文件名(绝对路径)
    private static final String sendKondorShellName = basePath
            + "printFileName.sh";

    public int executeShell(String shellCommand) throws IOException {
//        System.out.println("Spark Job Submission:"+shellCommand);
        int success = 0;
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        // Date Format, for logging
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS ");

        try {
            stringBuffer.append(dateFormat.format(new Date()))
                    .append("Spark Job Submission ").append(shellCommand)
                    .append(" \r\n");
            Process pid = null;
            String[] cmd = { "/bin/sh", "-c", shellCommand };
            //给shell传递参数
            //String[] cmd = { "/bin/sh", "-c", shellCommand+" paramater" };
            // 执行Shell命令
            pid = Runtime.getRuntime().exec(cmd);
            if (pid != null) {
                stringBuffer.append("PID：").append(pid.toString())
                        .append("\r\n");
                // bufferedReader用于读取Shell的输出内容
                bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
                pid.waitFor();
            } else {
                stringBuffer.append("NO PID\r\n");
            }
            stringBuffer.append(dateFormat.format(new Date())).append(
                    "Job Submission Completed\r\nResult：\r\n");
            String line = null;
            // 读取Shell的输出内容，并添加到stringBuffer中
            while (bufferedReader != null
                    && (line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\r\n");
            }
            //System.out.println("stringBuffer:"+stringBuffer);
        } catch (Exception ioe) {
            stringBuffer.append("Exception：\r\n").append(ioe.getMessage())
                    .append("\r\n");
        } finally {
            if (bufferedReader != null) {
                OutputStreamWriter outputStreamWriter = null;
                try {
                    bufferedReader.close();
                    // 将Shell的执行情况输出到日志文件中
                    OutputStream outputStream = new FileOutputStream(executeShellLogFile);
                    outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                    outputStreamWriter.write(stringBuffer.toString());
                //    System.out.println("stringBuffer.toString():"+stringBuffer.toString());
                    System.out.println(stringBuffer.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    outputStreamWriter.close();
                }
            }
            success = 1;
        }
        return success;
    }

    public static void main(String[] args) {
        try {
            new SparkJobSubmit().executeShell(sendKondorShellName+" 1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
