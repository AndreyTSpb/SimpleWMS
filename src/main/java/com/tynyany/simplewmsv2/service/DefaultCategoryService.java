package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.dao.CategoryEntity;
import com.tynyany.simplewmsv2.dao.CategoryRepository;
import com.tynyany.simplewmsv2.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultCategoryService implements CategoryService{
    final CategoryRepository categoryRepository;
    @Override
    public Category getByID(int categoryID) {
        return null;
    }

    @Override
    public List<Category> getAll() {
        return null;
    }

    @Override
    public void addCategory(Category category) {
        CategoryEntity categoryEntity = new CategoryEntity(
                0,
                category.getCategoryName(),
                category.getDescription(),
                false

        );
        categoryRepository.save(categoryEntity);
    }

    @Override
    public void updateCategory(Category category) {

    }

    @Override
    public void delCategory(Category category) {

    }
}
