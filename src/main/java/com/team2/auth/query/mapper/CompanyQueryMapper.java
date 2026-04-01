package com.team2.auth.query.mapper;

import com.team2.auth.command.domain.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyQueryMapper {

    Company findFirst();
}
