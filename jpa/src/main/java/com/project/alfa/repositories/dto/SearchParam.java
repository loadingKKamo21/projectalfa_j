package com.project.alfa.repositories.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SearchParam {
    
    private String       searchCondition;   //검색 조건
    private String       searchKeyword;     //검색 키워드
    private List<String> keywords;          //공백 기준 검색 키워드 분리 목록
    
    public SearchParam(String searchCondition, String searchKeyword) {
        this.searchCondition = searchCondition;
        this.searchKeyword = searchKeyword;
        this.keywords = searchKeyword == null ? new ArrayList<>() : Arrays.asList(searchKeyword.split("\\s+"));
    }
    
}
