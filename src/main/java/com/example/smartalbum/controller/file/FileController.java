package com.example.smartalbum.controller.file;

import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.FileOperationException;
import com.example.smartalbum.service.OssService;
import com.example.smartalbum.service.UpdateService;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件列表、删除文件、新建文件夹、进入文件夹
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private OssService ossService;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private UpdateService updateService;

    /**
     * 创建固定数量可重用线程池
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(50);

    /**
     * 新建文件夹 暂时用不上
     *
     * @param path          当前所在路径
     * @param newFolderName 新文件夹的名字
     */
    @Deprecated
    @PostMapping("/createFolder")
    public String newFolder(@RequestParam("path") String path,
                            @RequestParam("newFolderName") String newFolderName,
                            HttpSession session) throws IOException {
        //当前文件文件夹
        User user = (User) session.getAttribute("userInfo");
        String depositoryName = user.getDepository().getName();

        //新文件夹总路径
        String filepath = depositoryName + "/" + path + newFolderName + "/";

        if (ossService.checkFile(filepath)) {
            throw new FileExistsException("文件或文件夹不能重名,或文件已在当前目录存在");
        } else {
            ossService.createFolder(filepath);
        }
        return ResponseMsgUtil.success("创建文件夹成功！");
    }

    /**
     * 批量删除文件
     *
     * @param filenames 文件名字数组
     */
    @PostMapping("/delete")
    public String fileDelete(@RequestParam("filenames") String[] filenames,
                             HttpSession session) {

        if (filenames.length <= 0) {
            throw new FileOperationException("请选择要删除的文件！");
        }

        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();
        String depositoryName = user.getDepository().getName();


        CountDownLatch countDownLatch = new CountDownLatch(filenames.length);
        List<String> names = new ArrayList<>();
        for (String filename : filenames) {
            executorService.execute(()->{
                boolean result = imageDataService.deleteImage(depositoryId, filename)
                && ossService.deleteFile(depositoryName + "/" + filename, true);
                if (!result){
                    log.error("删除 {} 时失败！",filename);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("删除失败，请重试!");
        }
        updateService.updateUserInfo(session);
        return ResponseMsgUtil.success("成功删除以下文件", names);
    }

    /**
     * 重命名文件
     *
     * @param oldName 旧文件名
     * @param newName 新文件名
     */
    @PostMapping("/rename")
    @ResponseBody
    public String rename(@RequestParam("oldName") String oldName,
                         @RequestParam("newName") String newName,
                         HttpSession session) {
        User userInfo = (User) session.getAttribute("userInfo");
        int depositoryId = userInfo.getDepository().getId();
        String depositoryName = userInfo.getDepository().getName();
        String oldKey = depositoryName + "/" + oldName;
        String newKey = depositoryName + "/" + newName;
        if (ossService.rename(depositoryId, oldKey, newKey, depositoryName)) {
            return ResponseMsgUtil.success("重命名成功！");
        } else {
            throw new FileOperationException("重命名文件失败，请稍后再试！");
        }
    }

    /**
     * 获取当前用户的所有图片文件
     */
    @GetMapping("/list")
    public String getFileList(HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        List<Image> imageList = imageDataService.getSimpleImages(depositoryId);

        return ResponseMsgUtil.success(imageList);
    }

    /**
     * 获取某张图片的详情 如"/file/detail/1.jpg" 可获取1.jpg 的详情
     *
     * @param filename 文件名字
     */
    @GetMapping("/detail")
    public String getImageDetail(@RequestParam("filename") String filename, HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        Image image = imageDataService.getFullImage(filename, depositoryId);
        log.info("你点击了图片 {}",filename);

        return ResponseMsgUtil.success("获取图片数据成功！", image);
    }

}
