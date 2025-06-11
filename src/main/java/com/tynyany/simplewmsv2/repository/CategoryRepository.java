package com.tynyany.simplewmsv2.repository;

import com.tynyany.simplewmsv2.dao.CategoryEntity;
import com.tynyany.simplewmsv2.entity.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.CrudRepository;


public interface CategoryRepository extends CrudRepository<CategoryEntity, Integer> {
    Category findByCategoryName(String categoryName);
}
