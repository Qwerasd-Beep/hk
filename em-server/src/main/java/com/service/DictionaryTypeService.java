package com.service;

import com.entity.DictionaryType;

import java.util.List;

public interface DictionaryTypeService {
    List<DictionaryType> getAllTypes();
    DictionaryType getTypeById(Integer id);
    int addType(DictionaryType dictionaryType);
    int updateType(DictionaryType dictionaryType);
    int deleteType(Integer id);

}
