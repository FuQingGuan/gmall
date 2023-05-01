package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/1 12:48
 * @Email: moumouguan@gmail.com
 */
//@RestController
@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public String search(SearchParamVo paramVo, Model model) {
        SearchResponseVo responseVo = searchService.search(paramVo);

        model.addAttribute("response", responseVo);
        model.addAttribute("searchParam", paramVo);
        return "search";
    }
}
