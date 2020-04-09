package com.yamada.weibo.service;

import java.util.Set;

public interface SearchService {

    void addSearch(String content);

    Set<String> hotSearch();

    Set<String> candidate(String content);
}
