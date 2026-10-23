package com.service.impl;

import com.entity.DictionaryItem;
import com.init.DataLoader;
import com.mapper.DictionaryItemMapper;
import com.service.DictionaryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemServiceImpl implements DictionaryItemService {
    @Autowired
    DataLoader dataLoader;

    @Autowired
    private DictionaryItemMapper dictionaryItemMapper;

    public List<DictionaryItem> getItemsByTypeCode(String typeCode) {
        return dictionaryItemMapper.selectByTypeCode(typeCode);
    }

    public DictionaryItem getItemById(Integer id) {
        return dictionaryItemMapper.selectById(id);
    }

    public int addItem(DictionaryItem dictionaryItem) {
        int r=dictionaryItemMapper.insert(dictionaryItem);
        dataLoader.run();
        return r;
    }

    public int updateItem(DictionaryItem dictionaryItem) {
        int r=  dictionaryItemMapper.update(dictionaryItem);
        dataLoader.run();
        return r;
    }

    public int deleteItem(Integer id) {
        int r= dictionaryItemMapper.deleteById(id);
        dataLoader.run();
        return r;
    }
}

