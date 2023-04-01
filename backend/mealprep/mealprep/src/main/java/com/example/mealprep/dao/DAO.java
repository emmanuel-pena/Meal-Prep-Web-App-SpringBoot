package com.example.mealprep.dao;

import java.util.*;
import java.util.Optional;

public interface DAO<T> {

    List<T> list();

    void create(T t);

    Optional<T> get(int id);

    void update(T t, int id);

    void delete (int id);

}
