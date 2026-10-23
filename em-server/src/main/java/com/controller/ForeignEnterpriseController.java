package com.controller;

import com.entity.EnterpriseType;
import com.entity.ForeignEnterprise;
import com.entity.R;
import com.github.pagehelper.PageInfo;
import com.entity.query.ForeignEnterpriseQ;
import com.service.ForeignEnterpriseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foreign-enterprise")
public class ForeignEnterpriseController {
    @Resource
    private ForeignEnterpriseService foreignEnterpriseService;
    @GetMapping("/all")
    public R selectAll(@RequestParam(value="pageIndex",required = false,defaultValue = "1") Integer pageIndex, ForeignEnterpriseQ q) {
        PageInfo <ForeignEnterprise> pageInfo = foreignEnterpriseService.selectAll(pageIndex,q);
        return R.OK(pageInfo);
    }
    @GetMapping("/enterprise-type")
    public R loadEnterpriseType(){
        List <EnterpriseType> typeList = foreignEnterpriseService.loadEnterpriseTypes();
        return R.OK(typeList);
    }
    @RequestMapping("/add")
    public R add(@RequestBody ForeignEnterprise foreignEnterprise){
        foreignEnterpriseService.insert(foreignEnterprise);
        return R.OK();
    }
    @GetMapping("/{id}")
    public R selectById(@PathVariable Integer id){
        return R.OK(foreignEnterpriseService.selectById(id));
    }
    @RequestMapping("/update")
    public R update(@RequestBody ForeignEnterprise foreignEnterprise){
        foreignEnterpriseService.update(foreignEnterprise);
        return R.OK(foreignEnterprise);
    }
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Integer id){
        foreignEnterpriseService.deleteById(id);
        return R.OK();
    }


}
