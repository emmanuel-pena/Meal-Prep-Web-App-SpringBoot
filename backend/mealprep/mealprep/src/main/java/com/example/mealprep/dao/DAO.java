package com.example.mealprep.dao;

import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.Optional;

public interface DAO<T> {

    List<T> list();

    ResponseEntity create(T t);

    Optional<T> get(int id);

    void update(T t, int id);

    void delete (int id);

}
