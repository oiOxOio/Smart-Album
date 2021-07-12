package com.example.smartalbum.util;

import com.example.smartalbum.exception.ImageGenerateException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * 检测图片、图片压缩、生成缩略图、图片合成视频
 *
 * @author Administrator
 */
@Slf4j
@Component
public class ImageUtil {

    @Resource
    private VideoUtil videoUtil;

    /**
     * 检测图片格式
     *
     * @param fileName 图片名字
     * @return 是否为图片
     */
    public boolean checkImage(String fileName) {
        String suf = fileName.substring(fileName.indexOf('.') + 1);
        String[] types = {"jpg", "png", "jpeg","jfif" , "tif", "bmp", "gif", "svg", "psd", "ai", "raw", "wmf"};
        for (String s : types) {
            if (s.equalsIgnoreCase(suf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 压缩图片至指定大小，采用循环压缩
     *
     * @param srcImgData 被压缩的图片
     * @param maxSize    最大文件大小，单位byte
     * @return 压缩后的图片
     */
    public byte[] compressUnderSize(byte[] srcImgData, long maxSize) throws ImageGenerateException {
        double scale = 0.9;
        byte[] imgData = Arrays.copyOf(srcImgData, srcImgData.length);
        if (imgData.length > maxSize) {
            do {
                imgData = compress(imgData, scale);
            } while (imgData.length > maxSize);
        }
        return imgData;
    }

    /**
     * 缩放图片
     *
     * @param srcImgData 原图
     * @param scale      倍率
     * @return 压缩后的图片
     * @throws IOException
     */
    private static byte[] compress(byte[] srcImgData, double scale) throws ImageGenerateException {
        BufferedImage bi;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(srcImgData));
        } catch (IOException e) {
            throw new ImageGenerateException("图片读取失败！", e);
        }
        //新生成图片的高度宽度
        int width = (int) (bi.getWidth() * scale);
        int height = (int) (bi.getHeight() * scale);

        Image image = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 绘制处理后的图
        Graphics g = tag.getGraphics();
        g.setColor(Color.RED);
        g.drawImage(image, 0, 0, null);
        g.dispose();

        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();) {
            ImageIO.write(tag, "JPEG", bOut);
            return bOut.toByteArray();
        } catch (IOException e) {
            throw new ImageGenerateException("处理图片失败", e);
        }

    }

    /**
     * 生成图片的缩略图
     *
     * @param bytes 原图
     * @param width 等比缩放至指定宽度
     * @return 缩略图
     */
    public byte[] getMiniImage(byte[] bytes, double width) throws ImageGenerateException {

        BufferedImage originalImage;
        int imageWidth;

        try (InputStream in = new ByteArrayInputStream(bytes)) {
            originalImage = ImageIO.read(in);
        } catch (IOException e) {
            throw new ImageGenerateException("图片读取失败！", e);
        }

        imageWidth = originalImage != null ? originalImage.getWidth() : 0;
        //根据自适应缩放,调整宽度始终为width
        double rate = imageWidth / width;
        double scaleRate = 1 / rate;

        BufferedImage mini;
        try {
            mini = Thumbnails.of(originalImage).scale(scaleRate).asBufferedImage();
        } catch (IOException e) {
            throw new ImageGenerateException("处理缩略图失败！", e);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            //写进输出流
            ImageIO.write(mini, "png", outputStream);
            //转换成字节数组
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageGenerateException("输出缩略图失败！", e);
        }
    }

    /**
     * 生成视频帧，每帧逐渐放大
     *
     * @param file  原图
     * @param scale 生成的图片最终放大到的倍数
     * @param count 生成多少张，对应视频帧数
     */
    public void createFrames(File file, double scale, int count) throws ImageGenerateException {
        BufferedImage bi;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            throw new ImageGenerateException("图片读取失败！", e);
        }

        String path = file.getPath();
        String pre = path.substring(0, path.lastIndexOf("."));
        String sub = path.substring(path.lastIndexOf("."));

        int oWidth = bi.getWidth();
        int oHeight = bi.getHeight();
        double currentScale = 1;
        double temp = (scale - currentScale) / count;
        int width;
        int height;

        Image image;
        Graphics g;
        for (int i = 0; i < count && currentScale < scale; i++, currentScale += temp) {
            File output = new File(pre + "_" + i + sub);
            //跳过已经生成过的图片
            if (output.exists()) {
                continue;
            }
            //新生成图片的高度宽度
            width = (int) (oWidth * currentScale);
            height = (int) (oHeight * currentScale);
            image = bi.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            bi = new BufferedImage(oWidth, oHeight, BufferedImage.TYPE_INT_RGB);

            // 绘制处理后的图
            g = bi.getGraphics();
            g.drawImage(image, (oWidth - width) / 2, (oHeight - height) / 2, null);
            g.dispose();
            try {
                ImageIO.write(bi, sub.replace(".", ""), output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 多张图片合成为视频
     *
     * @param inputPath  存放图片的文件夹路径，默认是该文件夹所有图片都合成为一整个视频
     * @param outputPath 用来存放生成的视频的文件夹，完整文件夹路径
     */
    public void imagesToVideo(String inputPath, String outputPath) {
        long start = System.currentTimeMillis();
        log.info("正在生成视频");

        //如果输出文件夹不存在则创建新文件夹
        File video = new File(outputPath);
        if (!video.exists()) {
            if (video.mkdirs()) {
                log.info("创建 {} 成功",outputPath);
            }
        }

        try {
            //1、图片转化为视频并加入淡入淡出转场
            videoUtil.createMp4(inputPath, outputPath);
            //2、生成待合并视频的文件列表merge.txt，合并视频时以此文件为根据进行合并
            videoUtil.createMargeTxt(outputPath);
            //3、根据marge.txt 合成所有视频片段，生成最终的视频mp4文件
            videoUtil.mergeVideo(outputPath);
        } catch (IOException | InterruptedException e) {
            File[] files = video.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        log.info("删除临时文件 {}",file.getPath());
                    }
                }
            }
        }

        log.info("生成视频完毕，耗时:" + (System.currentTimeMillis() - start) + "ms");
    }
}
