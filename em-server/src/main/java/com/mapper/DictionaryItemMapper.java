package com.mapper;

import com.entity.DictionaryItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictionaryItemMapper {
    List<DictionaryItem> selectByTypeCode(String typeCode);
    DictionaryItem selectById(Integer id);
    int insert(DictionaryItem dictionaryItem);
    int update(DictionaryItem dictionaryItem);
    int deleteById(Integer id);

    //从字典项中查询所有字典条目, 用在把字典加载到缓存中的功能里
    List<DictionaryItem> selectAll();
}