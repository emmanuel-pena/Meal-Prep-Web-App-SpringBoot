package com.example.mealprep.dao;
import com.example.mealprep.model.Favoriterecipe;
import com.example.mealprep.model.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;
import org.postgresql.util.PGobject;
import com.google.gson.Gson;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Qualifier("FavoriteRecipeJdbcDAO")
public class FavoriteRecipeJdbcDAO implements DAO {

    private final JdbcTemplate jdbcTemplate;

    RowMapper<Favoriterecipe> rowMapper = (rs, rowNum) -> {
        Favoriterecipe favRecipe = new Favoriterecipe();

        favRecipe.setId(rs.getString("id"));
        favRecipe.setMemberId(rs.getString("member_id"));
        favRecipe.setRecipeId(rs.getInt("recipe_id"));
        return favRecipe;
    };


    @Override
    public List list() {
        String sql = "SELECT * FROM favorite_recipes";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public ResponseEntity create(Object o) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public Optional get(int id) {
        return Optional.empty();
    }

    @Override
    public void update(Object o, int id) {

    }

    @Override
    public void delete(int id) {

    }

    @Transactional
    public ResponseEntity<Object> addToFavorites(HashMap<String, Object> info, String id) {

        String memberId = id;

        HashMap<String,Object> recipeObj = (HashMap<String, Object>) info.get("RecipeObj");

        int recipeId = Integer.valueOf(info.get("recipeId").toString());

        int rows;

        // to use for any results: CONFLICT AND OK
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("memberId", memberId);
        responseBody.put("recipeId", recipeId);

        Boolean alreadyInFavorites = searchExistingFavorites(memberId, recipeId);

        if (alreadyInFavorites == true) {
           return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
        }
        else {
            // insert into recipes first if needed
            insertIntoRecipes(recipeId, recipeObj);

            // insert into user's favorite recipes
            String sql = "INSERT INTO favorite_recipes (member_id, recipe_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            rows = jdbcTemplate.update(sql, UUID.fromString(memberId), recipeId);
        }

        if (rows > 0) {
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }

    }

    private void insertIntoRecipes(int recipeId, HashMap<String, Object> recipeObj) {

        Gson gson = new Gson();
        String jsonString = gson.toJson(recipeObj);

        PGobject jsonb = new PGobject();

        try {
            jsonb.setType("jsonb");
            jsonb.setValue(jsonString);
        }
        catch (SQLException e) {
            System.out.println(e);
        }

        String sql = "INSERT INTO recipe (id, info) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, recipeId, jsonb);
    }

    private Boolean searchExistingFavorites(String memberId, int recipeId) {
        String sql = "SELECT * FROM favorite_recipes WHERE member_id = \'" + memberId
                + "\' AND recipe_id = " + recipeId;

        Favoriterecipe recipe = null;

        try {
            recipe = jdbcTemplate.queryForObject(sql, rowMapper);
            return true;
        }
        catch (EmptyResultDataAccessException ex) {
            return false;
        }

    }

    public ResponseEntity<Object> getAllFromFavorites(String memberId) {
        String sql
                = "SELECT DISTINCT r.info FROM recipe r, favorite_recipes f WHERE r.id = f.recipe_id AND f.member_id = ?";

        List<PGobject> jsonbList = jdbcTemplate.queryForList(sql, PGobject.class, UUID.fromString(memberId));

        if (jsonbList == null) {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }
        List<String> jsonbStringList = new ArrayList<>();
        List<Object> recipeObjList = new ArrayList<>();

        jsonbList.forEach(
                (item) -> { jsonbStringList.add(item.getValue()); }
        );

        jsonbStringList.forEach(
                (item) -> {
                    HashMap<String, Object> recipeObj = new Gson().fromJson(item, HashMap.class);
                    recipeObjList.add(recipeObj);
                }
        );

        return new ResponseEntity<>(recipeObjList, HttpStatus.OK);
    }


    public ResponseEntity<Object> deleteFromFavorites(String memberId, String recipeId) {

        int rows = jdbcTemplate.update("DELETE FROM favorite_recipes WHERE member_id = ? AND recipe_id = ?", UUID.fromString(memberId), recipeId);

        if (rows > 0) {
            return new ResponseEntity<>("Ok. Deleted.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }
    }

}
