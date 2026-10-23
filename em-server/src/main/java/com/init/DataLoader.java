package com.init;

import com.entity.DictionaryItem;
import com.mapper.DictionaryItemMapper;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataLoader implements CommandLineRunner {
    @Resource
    private DictionaryItemMapper dictionaryItemMapper;
    public static Map<String, List<DictionaryItem>> globalDicMap;
    @Override
    public void run(String... args)  {
        List<DictionaryItem> dictItems = dictionaryItemMapper.selectAll();
        globalDicMap = dictItems.stream().collect(Collectors.groupingBy(item->item.get类型编码()));
//        globalDicMap.forEach((k,v)->{
//            System.out.println(k);
//            for(DictionaryItem dictItem:v){
//                System.out.println(dictItem.getId()+""+dictItem.get字典项());
//            }
//        });

    }
}
