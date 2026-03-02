package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.demo.model.Todo;

public interface TodoMapper {
    List<Todo> findAll();

    Todo findById(Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int requestDelete(Long id);

    int approveDelete(@Param("id") Long id, @Param("deletedAt") java.time.LocalDateTime deletedAt);

    List<Todo> findHistory();

    int deleteById(Long id);
}
