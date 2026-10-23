package com.service.impl;

import com.aop.DataAccess;
import com.constant.Const;
import com.entity.ApprovalProcessInfo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mapper.ApprovalProcessInfoMapper;
import com.service.ApprovalProcessInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalProcessInfoServiceImpl implements ApprovalProcessInfoService {
    @Autowired
    private ApprovalProcessInfoMapper mapper;

    @Override
    public int insert(ApprovalProcessInfo o) {
        return mapper.insert(o);
    }

    @Override
    public int deleteById(int id) {
        return mapper.deleteById(id);
    }

    @Override
    public int update(ApprovalProcessInfo o) {
        return mapper.update(o);
    }

    @Override
    public ApprovalProcessInfo selectById(int id) {
        return mapper.selectById(id);
    }

    //先不要加,以后再加上下面的注解
     @DataAccess(tableAlias = "a",fieldName = "ownerId")
    public PageInfo<ApprovalProcessInfo> selectAll(ApprovalProcessInfo q, Integer pageIndex) {
        PageHelper.startPage(pageIndex, Const.PAGE_SIZE);
        List<ApprovalProcessInfo> list= mapper.selectAll(q);
        return new PageInfo<>(list);
    }
}
