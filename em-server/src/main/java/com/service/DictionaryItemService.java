package com.service;

import com.entity.DictionaryItem;

import java.util.List;

public interface DictionaryItemService {
    List<DictionaryItem> getItemsByTypeCode(String typeCode);
    DictionaryItem getItemById(Integer id);
    int addItem(DictionaryItem dictionaryItem);
    int updateItem(DictionaryItem dictionaryItem);
    int deleteItem(Integer id);

}
