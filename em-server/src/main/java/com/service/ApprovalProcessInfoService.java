package com.service;

import com.entity.ApprovalProcessInfo;
import com.github.pagehelper.PageInfo;

public interface ApprovalProcessInfoService {
    int insert(ApprovalProcessInfo o);
    int deleteById(int id);
    int update(ApprovalProcessInfo o);
    ApprovalProcessInfo selectById(int id);

    PageInfo<ApprovalProcessInfo> selectAll(ApprovalProcessInfo q,Integer pageIndex);
}

