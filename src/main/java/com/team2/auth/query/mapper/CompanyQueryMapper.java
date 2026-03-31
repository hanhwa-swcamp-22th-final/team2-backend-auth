package com.team2.auth.query.mapper;

import com.team2.auth.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyQueryMapper {

    Company findFirst();
}
