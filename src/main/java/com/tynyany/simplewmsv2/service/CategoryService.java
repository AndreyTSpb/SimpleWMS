package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.Category;

import java.util.List;

public interface CategoryService {
    Category getByID(int categoryID);
    List<Category> getAll();
    int addCategory(Category category);
    void updateCategory(Category category);
    void delCategory(Category category);
}
