package com.mapper;

import com.entity.DictionaryType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictionaryTypeMapper {
    List<DictionaryType> selectAll();
    DictionaryType selectById(Integer id);
    int insert(DictionaryType dictionaryType);
    int update(DictionaryType dictionaryType);
    int deleteById(Integer id);
}
