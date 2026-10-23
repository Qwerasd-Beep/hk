package com.controller;

import com.entity.DictionaryItem;
import com.entity.DictionaryType;
import com.entity.R;
import com.init.DataLoader;
import com.service.DictionaryItemService;
import com.service.DictionaryTypeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DictionaryController {
    @Autowired
    private DictionaryTypeService dicTypeService;

    @Autowired
    private DictionaryItemService dicItemService;

    // 获取所有字典类型
    @GetMapping("/dictionary-types/getall")
    public R getAllTypes(HttpServletRequest request) {
        // String token=request.getHeader("auth_token");
        // UserInfo user=JwtUtil.parseUserFromJWT(token);
        List<DictionaryType> types = dicTypeService.getAllTypes();
        return R.OK(types);
    }

    // 根据 ID 获取字典类型
    @GetMapping("/dictionary-types/getone/{id}")
    public DictionaryType getTypeById(@PathVariable Integer id) {
        return dicTypeService.getTypeById(id);
    }

    // 新增字典类型
    @PostMapping("/dictionary-types/add")
    public R addType(@RequestBody DictionaryType dictionaryType) {
        dicTypeService.addType(dictionaryType);
        return R.OK();
    }

    // 更新字典类型
    @PutMapping("/dictionary-types/update")
    public R updateType(@RequestBody DictionaryType dictionaryType) {
        dicTypeService.updateType(dictionaryType);
        return R.OK();
    }

    // 删除字典类型
    @DeleteMapping("/dictionary-types/delete/{id}")
    public R deleteType(@PathVariable Integer id) {
        dicTypeService.deleteType(id);
        return R.OK();
    }

    // 根据类型编码获取字典项
    @GetMapping("/dictionary-items/getbycode/{typeCode}")
    public R getItemsByTypeCode(@PathVariable String typeCode) {
        List<DictionaryItem> list = dicItemService.getItemsByTypeCode(typeCode);
        return R.OK(list);
    }

    // 根据 ID 获取字典项
    @GetMapping("/dictionary-items/getone/{id}")
    public DictionaryItem getItemById(@PathVariable Integer id) {
        return dicItemService.getItemById(id);
    }

    // 新增字典项
    @PostMapping("/dictionary-items/add")
    public R addItem(@RequestBody DictionaryItem dictionaryItem) {
        dicItemService.addItem(dictionaryItem);
        return R.OK();
    }

    // 更新字典项
    @PutMapping("/dictionary-items/update")
    public R updateItem(@RequestBody DictionaryItem dictionaryItem) {
        dicItemService.updateItem(dictionaryItem);
        return R.OK();
    }

    // 删除字典项
    @DeleteMapping("/dictionary-items/delete/{id}")
    public R deleteItem(@PathVariable Integer id) {
        dicItemService.deleteItem(id);
        return R.OK();
    }

    // 直接从数据字典获取某个类型的所有字典项
    @GetMapping("/dictionary-items/get-dic-bycode/{typeCode}")
    public R getItemsFromCache(@PathVariable String typeCode) {
        List<DictionaryItem> list = DataLoader.globalDicMap.get(typeCode);
        return R.OK(list);
    }
}
