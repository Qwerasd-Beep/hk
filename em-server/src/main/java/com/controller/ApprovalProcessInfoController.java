package com.controller;

import com.entity.ApprovalProcessInfo;
import com.entity.R;
import com.github.pagehelper.PageInfo;
import com.service.ApprovalProcessInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval-processInfo")
public class ApprovalProcessInfoController {
    @Resource
    private ApprovalProcessInfoService service;

    @PostMapping("/insert")
    public R insert(@RequestBody ApprovalProcessInfo o) {
        service.insert(o);
        return R.OK();
    }

    @DeleteMapping("/{id}")
    public R deleteById(@PathVariable int id) {
        service.deleteById(id);
        return R.OK();
    }

    @PutMapping("/update")
    public R update(@RequestBody ApprovalProcessInfo o) {
        service.update(o);
        return R.OK();
    }

    @GetMapping("/{id}")
    public R selectById(@PathVariable int id) {
        ApprovalProcessInfo o = service.selectById(id);
        return R.OK(o);
    }

    //上面那些方法都没用上,下面这个是关键
    @GetMapping("/all")
    public R selectAll(@RequestParam(value = "pageIndex", required = false,defaultValue = "1") Integer pageIndex,ApprovalProcessInfo q) {
        PageInfo<ApprovalProcessInfo> pageInfo = service.selectAll(q,pageIndex);
        return R.OK(pageInfo);
    }
}

