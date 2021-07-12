package com.example.smartalbum.controller.page;

import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.ImageSetDataService;
import com.example.smartalbum.util.RandomStringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Controller
public class WonderfulController {
    @Resource
    private ImageSetDataService imageSetDataService;

    @GetMapping("/wonderful")
    public String wonderful(@RequestParam("name") String name, HttpSession session, Model model) {
        User user = (User) session.getAttribute("userInfo");
        List<ImageSet> imageSetList = imageSetDataService.getImageSets(user.getDepository().getId());
        List<ImageSet> imageSets = imageSetList.stream()
                .filter((imageSet) -> imageSet.getName().equals(name))
                .collect(Collectors.toList());

        int i;
        do {
            i = RandomStringUtil.random.nextInt(imageSetList.size());
            imageSets.add(imageSetList.get(i));
        }while (imageSets.size() < 3);

        model.addAttribute("imageSetList",imageSets);
        return "wonderful";
    }
}
