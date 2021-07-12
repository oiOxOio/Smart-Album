package com.example.smartalbum.controller.page;

import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.ImageSetDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Administrator
 */
@Controller
public class AlbumController {

    @Resource
    private ImageSetDataService imageSetDataService;

    @GetMapping("/album")
    public String album(Model model, HttpSession session) {

        //把session中的用户信息放入model中
        UserMainController.putUserinfoIntoTheModel(model, session);

        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();
        List<ImageSet> imageSetList = imageSetDataService.queryImageSet(depositoryId);
        model.addAttribute("imageSetList", imageSetList);
        return "album";
    }

    @GetMapping("/album/{imageSetName}")
    public String album(@PathVariable("imageSetName") String imageSetName,
                        Model model,
                        HttpSession session) {
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        int imageSetId = imageSetDataService.getImageSetId(imageSetName, depositoryId);

        ImageSet imageSet = imageSetDataService.getImageSet(imageSetId);

        //把session中的用户信息放入model中
        UserMainController.putUserinfoIntoTheModel(model, session);

        model.addAttribute("imageSet", imageSet);
        return "albumContent";
    }
}
