package com.controller;

import com.entity.R;
import com.service.LocalProjectInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-project-info")
public class LocalProjectInfoController {

    @Resource
    private LocalProjectInfoService localProjectInfoService;

    @GetMapping("/investment-by-year")
    public R sumInvestmentByYear() {
        return R.OK(localProjectInfoService.sumInvestmentByYear());
    }
}
