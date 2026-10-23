package com.controller;

import com.entity.DictionaryItem;
import com.entity.IntermediaryAgency;
import com.entity.R;
import com.github.pagehelper.PageInfo;
import com.init.DataLoader;
import com.service.IntermediaryAgencyService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/agencies")
public class IntermediaryAgencyController {
    @Resource
    private IntermediaryAgencyService agencySerivce;


    @PreAuthorize(value = "hasAuthority('zhongjie_jigou:list')")
    @GetMapping("/all")
    public R getAll(
            @RequestParam(value = "pageIndex", required = false,defaultValue = "1") Integer pageIndex, IntermediaryAgency q) {
        PageInfo<IntermediaryAgency> pageInfo = agencySerivce.findAll(pageIndex,q);
        return  R.OK(pageInfo);
    }

    @GetMapping("/get-byame/{agencyName}")
    public R getAgencyByName(@PathVariable("agencyName") String agencyName) {
        IntermediaryAgency agency = agencySerivce.findByAgencyName(agencyName);
        return  R.OK(agency);
    }

    @GetMapping("/{id}")
    public  R getAgencyById(@PathVariable("id") Integer id) {
        IntermediaryAgency agency = agencySerivce.findByAgencyId(id);
        return  R.OK(agency);
    }


    @PostMapping("/add")
    public R addAgency(@RequestBody IntermediaryAgency agency) {
        agencySerivce.insert(agency);
        return  R.OK();
    }

    @PutMapping("/update")
    public  R updateAgency(@RequestBody IntermediaryAgency agency) {
        agencySerivce.update(agency);
        return  R.OK();
    }

    @PreAuthorize("hasAuthority('zhongjie_jigou:delete')")
    @DeleteMapping("/delete/{agencyName}")
    public R deleteAgency(@PathVariable("agencyName") String agencyName) {
        agencySerivce.deleteByAgencyName(agencyName);
        return  R.OK();
    }

    @GetMapping("/dictionary-items/get-dict-bycode/{typeCode}")
    public R getItemsFromCahce(@PathVariable("typeCode") String typeCode) {
        List< DictionaryItem> list = DataLoader.globalDicMap.get(typeCode);
        return  R.OK(list);
    }

}
