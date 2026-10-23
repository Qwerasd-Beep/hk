package com.service.impl;

import com.entity.DictionaryType;
import com.init.DataLoader;
import com.mapper.DictionaryTypeMapper;
import com.service.DictionaryTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryTypeServiceImpl implements DictionaryTypeService {
    @Autowired
    DataLoader dataLoader;

    @Autowired
    private DictionaryTypeMapper dictionaryTypeMapper;

    public List<DictionaryType> getAllTypes() {
        return dictionaryTypeMapper.selectAll();
    }

    public DictionaryType getTypeById(Integer id) {
        return dictionaryTypeMapper.selectById(id);
    }

    public int addType(DictionaryType dictionaryType) {
        int r=   dictionaryTypeMapper.insert(dictionaryType);
        dataLoader.run();
        return r;
    }

    public int updateType(DictionaryType dictionaryType) {
        int r= dictionaryTypeMapper.update(dictionaryType);
        dataLoader.run();
        return r;
    }

    public int deleteType(Integer id) {
        int r= dictionaryTypeMapper.deleteById(id);
        dataLoader.run();
        return r;
    }

}
