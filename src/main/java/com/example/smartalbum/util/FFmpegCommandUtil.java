package com.example.smartalbum.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @date 2021/6/7 13:47
 */
@Component
public class FFmpegCommandUtil {

    @Value("${app.ffmpegPath}")
    private String ffmpeg;

    /**
     * 根据ts文件合并多个视频
     */
    public String mergeVideo(String txtPath, String outPath) {
        return ffmpeg + " -f concat -safe 0 -i " + txtPath + " -c copy " + outPath;
    }

    /**
     * 图片转化为视频，并加入淡入淡出效果
     *
     * @param inputPath  要转化的图片的完整路径
     * @param outputPath 输出的视频的完整路径
     */
    public String createFadeVideo(String inputPath, String outputPath) {
        return ffmpeg +
                " -r " + 24 +
                " -t " + 4 +
                " -loop " + 1 +
                " -i \"" + inputPath + "\"" +
                //第0帧开始淡入15帧，到70帧的时候淡出15帧，保持1280x720分辨率，并自动裁剪多余部分
                " -vf fade=in:0:15:color=white,fade=out:70:15:color=white,scale=1280:720:force_original_aspect_ratio=increase,crop=1280:720 " +
                " -vcodec libx264 -pix_fmt yuv420p -acodec aac " +
                " -y " + outputPath;
    }
}
