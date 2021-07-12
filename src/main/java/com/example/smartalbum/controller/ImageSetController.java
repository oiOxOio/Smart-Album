package com.example.smartalbum.controller;

import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.AlbumOperationException;
import com.example.smartalbum.exception.TemporaryFileDoesNotExistException;
import com.example.smartalbum.service.OssService;
import com.example.smartalbum.service.UploadService;
import com.example.smartalbum.service.database.ImageSetDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/album")
public class ImageSetController {

    @Resource
    private OssService ossService;
    @Resource
    private UploadService uploadService;
    @Resource
    private ImageSetDataService imageSetDataService;

    @Value("${ecloud.resources}")
    private String resources;

    /**
     * 删除相册（相册删除后应该将image_set_id设置为1）
     */
    @PostMapping("/delete/{imageSetName}")
    public String deleteImageSetById(@PathVariable("imageSetName") String imageSetName,
                                     HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        int imageSetId = imageSetDataService.getImageSetId(imageSetName, depositoryId);

        int state = imageSetDataService.deleteImageSet(imageSetId, depositoryId);


        if (state > 0) {
            //删除相册后，把在本相册的图片的image_set_id改为1
            int count = imageSetDataService.updateImageSetId(imageSetId, depositoryId);
            return ResponseMsgUtil.success("删除相册成功!，并将" + count + "张图片移出了该相册");
        } else {
            throw new AlbumOperationException("删除相册失败！请重试！");
        }
    }

    /**
     * 将图片从当前相册里面移除
     */
    @PostMapping("/{imageSetName}/removeImage")
    public String removeImageForImageSet(@PathVariable("imageSetName") String imageSetName,
                                         @RequestParam("imageId[]") int[] imageId,
                                         HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        Set<Integer> set = new HashSet<>();
        for (int id : imageId) {
            set.add(id);
        }

        int imageSetId = imageSetDataService.getImageSetId(imageSetName, depositoryId);
        int num = imageSetDataService.removeImageForImageSet(set, depositoryId, imageSetId);

        if (num > 0) {
            return ResponseMsgUtil.success("成功移除 " + num + " 张图片");
        }
        return ResponseMsgUtil.failure();
    }

    /**
     * 输入相册名后进行验证
     *
     * @param imageSetName 相册名
     */
    @GetMapping("/checkname/{imageSetName}")
    public String checkName(@PathVariable("imageSetName") String imageSetName, HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        if (imageSetDataService.getImageSet(imageSetName, depositoryId).isEmpty()) {
            return ResponseMsgUtil.success("可以使用该相册名！");
        } else {
            throw new AlbumOperationException("该相册已存在！");
        }
    }

    /**
     * 获取默认的背景图片和用户已上传过的背景图片
     */
    @GetMapping("/backgrounds")
    public String backgrounds(HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        String depositoryName = user.getDepository().getName();

        //存放最终结果
        Map<String,Object> result = new HashMap<>();

        //公共背景
        List<String> list = ossService.traverseFileB2(resources + "/albumBackground");
        if (list != null && !list.isEmpty()) {
            list.removeIf(s -> s.charAt(s.length() - 1) == '/');
            //oss上 /resources/albumBackground 下的所有图片的url
            List<String> defaultUrls = new LinkedList<>();
            for (String objectName : list) {
                defaultUrls.add(ossService.createUrlB2(objectName) + "");
            }
            result.put("defaultBackground",defaultUrls);
        }

        //用户自己上传的背景图片
        List<String> customBackgrounds = ossService.traverseFileB2(resources + "/customBackground/" + depositoryName);
        if (customBackgrounds != null && !customBackgrounds.isEmpty()){
            customBackgrounds.removeIf(s -> s.charAt(s.length() - 1) == '/');
            //oss上 /resources/customBackground/depositoryName 下的所有图片的name和url
            Map<String,String> imageMap = new LinkedHashMap<>();
            for (String objectName : customBackgrounds) {
                imageMap.put(objectName,ossService.createUrlB2(objectName) + "");
            }
            result.put("customBackground",imageMap);
        }

        if (!result.isEmpty()) {
            return ResponseMsgUtil.success(result);
        }else {
            return ResponseMsgUtil.failure("图片资源加载失败！");
        }
    }

    /**
     * 获取相册外的图片
     * @param imageSetName 相册名字
     */
    @GetMapping("/outsideImage")
    public String outsideImage(@RequestParam("imageSetName")String imageSetName,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               HttpSession session){
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();
        int imageSetId = imageSetDataService.getImageSetId(imageSetName, depositoryId);
        PageInfo<Image> pageInfo = imageSetDataService.getImageOutsideImageSet(imageSetId, pageNum);
        return ResponseMsgUtil.success(pageInfo);
    }

    /**
     * 删除已经上传过的背景图片
     */
    @PostMapping("/deleteUploadedBackground")
    public String deleteUploadedBackground(@RequestParam("fileName") String fileName) {
        if (fileName == null || fileName.isEmpty()){
            return ResponseMsgUtil.failure("文件名不能为空");
        }
//        String objectName = resources + "/customBackground/" + depositoryName + "/" + fileName;
        boolean result = ossService.deleteFileB2(fileName);
        if (result){
            return ResponseMsgUtil.success();
        }else{
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 上传背景图片
     */
    @PostMapping("/uploadBackground")
    public String uploadBackground(MultipartFile[] multipartFiles,
                                   HttpSession session) throws IOException {
        //判断上传的文件是否为空
        if (multipartFiles.length <= 0 || multipartFiles[0].isEmpty()) {
            return ResponseMsgUtil.failure("上传文件为空!");
        }

        //去重 去空
        List<MultipartFile> fileList = Arrays.stream(multipartFiles)
                .distinct()
                .filter((item) -> !item.isEmpty())
                .collect(Collectors.toList());

        User user = (User) session.getAttribute("userInfo");
        String depositoryName = user.getDepository().getName();

        String objectName;
        //遍历
        for (MultipartFile multipartFile : fileList) {
            //上传
            objectName = resources + "/customBackground/" + depositoryName + "/" + multipartFile.getOriginalFilename();
            ossService.uploadImageB2(objectName,multipartFile.getInputStream());
        }
        return ResponseMsgUtil.success();
    }

    /**
     * 创建相册
     */
    @PostMapping("/create")
    public String addImageSet(@RequestParam("name") String name,
                              @RequestParam("summary") String summary,
                              @RequestParam("detail") String detail,
                              @RequestParam("backgroundUrl") String backgroundUrl,
                              MultipartFile backgroundFile,
                              HttpSession session) throws IOException {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        ImageSet imageSet = new ImageSet();
        imageSet.setName(name);
        imageSet.setSummary(summary);
        imageSet.setDetail(detail);

        if (!backgroundUrl.isEmpty()) {
            imageSet.setBackgroundUrl(backgroundUrl);
        } else if (backgroundFile != null && !backgroundFile.isEmpty()) {
            int length = backgroundFile.getBytes().length;
            String filename = backgroundFile.getOriginalFilename();
            //检测是否为图片，空间是否足够，是否重复
            if (uploadService.checkUploadFile(filename, length, session)) {

                String depositoryName = user.getDepository().getName();
                String objectName = depositoryName + "/" + resources + "/albumBackground/" + filename;

                if (ossService.uploadImageB2(objectName,backgroundFile.getInputStream())) {
                    imageSet.setBackgroundUrl(ossService.createUrlB2(objectName) + "");
                }
            }
        } else {
            imageSet.setBackgroundUrl("https://source.unsplash.com/random");
        }

        int num = imageSetDataService.insertSelective(imageSet, depositoryId);

        if (num > 0) {
            return ResponseMsgUtil.success("创建相册成功!");
        } else {
            throw new AlbumOperationException("创建相册失败!");
        }
    }

    /**
     * 修改相册信息
     */
    @PostMapping("/updateImageSet")
    public String updateImageSetForDepositoryIdAndId(ImageSet imageSet, HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        int num = imageSetDataService.updateImageSet(imageSet, depositoryId);

        if (num > 0) {
            return ResponseMsgUtil.success("修改相册信息成功!");
        } else {
            throw new AlbumOperationException("修改相册信息失败!");
        }
    }

    /**
     * 将图片添加到相册里面
     *
     * @param imageSetName 要添加到的相册的名字
     * @param imageId      待添加的图片id数组
     */
    @PostMapping("/{imageSetName}/addImage")
    public String addImageToImageSet(@PathVariable("imageSetName") String imageSetName,
                                     @RequestParam("imageId[]") int[] imageId,
                                     HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        Set<Integer> set = new HashSet<>();
        for (int id : imageId) {
            set.add(id);
        }

        //根据相册名字获取id
        int imageSetId = imageSetDataService.getImageSetId(imageSetName, depositoryId);
        int num = imageSetDataService.addImage(set, imageSetId, depositoryId);

        if (num > 0) {
            return ResponseMsgUtil.success();
        }
        return ResponseMsgUtil.failure();
    }

}
