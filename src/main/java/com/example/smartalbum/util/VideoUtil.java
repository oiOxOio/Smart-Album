package com.example.smartalbum.util;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Component
public class VideoUtil {

    @Resource
    private FFmpegCommandUtil fFmpegCommandUtil;

    /**
     * 1、把图片转化为一个有淡入淡出效果的ts视频(可改成后缀为mp4等任意视频，后缀ts是为了方便区分^_^)
     *
     * @param inputPath  图片的文件夹路径
     * @param outputPath ts文件输出的文件夹路径
     * @throws IOException          执行命令行命令时或者关闭流时发生的异常
     * @throws InterruptedException 等待子线程完成任务的过程中，子线程被中断时发生异常
     */
    public void createMp4(String inputPath, String outputPath) throws IOException, InterruptedException {
        File[] files = new File(inputPath).listFiles();
        if (files != null) {
            //图片转化为视频
            List<Process> processes = new ArrayList<>();
            String command;
            String img;
            String outputVideo;
            for (int i = 0; i < files.length; i++) {
                img = files[i].getAbsolutePath();
                outputVideo = outputPath + i + ".ts";
                command = fFmpegCommandUtil.createFadeVideo(img, outputVideo);
                processes.add(Runtime.getRuntime().exec(command));
            }

            //等待所有子线程执行完毕
            for (Process process : processes) {
                //不知道啥原因，不执行这些语句的话，主线程会一直被阻塞
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                //等待
                process.waitFor();
            }

        }
    }

    /**
     * 2、生成待合并视频的文件列表merge.txt，合并视频时以此文件为根据进行合并
     *
     * @param outputPath merge.txt存放的文件夹路径
     * @throws IOException 执行命令行命令时或者关闭流时发生的异常
     */
    public void createMargeTxt(String outputPath) throws IOException {
        //生成的待合成视频数组
        File[] videos = new File(outputPath).listFiles();
        if (videos != null) {
            String margePath = outputPath + "marge.txt";
            File file = new File(margePath);
            if (file.exists()) {
                file.delete();
            }

            //把所有生成的ts文件的完整路径写入marge.txt
            StringBuilder margeTxt = new StringBuilder();
            for (File video : videos) {
                if (video.length() >= 0) {
                    if (video.getName().contains(".ts")) {
                        //构建内容
                        margeTxt.append("file '").append(video.getAbsolutePath()).append("'\n");
                    }
                }
            }

            //写入文件
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                writer.write(String.valueOf(margeTxt));
            }
        }
    }

    /**
     * 3、根据marge.txt 合成所有视频片段，生成最终的视频mp4文件
     *
     * @param outputPath 输出的路径，不需要带文件名
     * @throws IOException          执行命令行命令时或者关闭流时发生的异常
     * @throws InterruptedException 等待子线程完成任务的过程中，子线程被中断时发生异常
     */
    public void mergeVideo(String outputPath) throws InterruptedException, IOException {

        String margeTxt = outputPath + "marge.txt";
        String outputVideo = outputPath + "out.mp4";
        String command = fFmpegCommandUtil.mergeVideo(margeTxt, outputVideo);

        Process exec = Runtime.getRuntime().exec(command);
        exec.getErrorStream().close();
        exec.getInputStream().close();
        exec.getOutputStream().close();
        exec.waitFor();

    }
}
