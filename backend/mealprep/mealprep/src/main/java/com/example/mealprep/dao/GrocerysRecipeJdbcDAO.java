package com.example.mealprep.dao;
import com.example.mealprep.model.Favoriterecipe;
import com.example.mealprep.model.Grocerylist;
import com.example.mealprep.model.Grocerysrecipe;
import com.google.gson.Gson;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.Optional;

@Component
@Qualifier("GrocerysRecipeJdbcDAO")
public class GrocerysRecipeJdbcDAO implements DAO {

    private final JdbcTemplate jdbcTemplate;

    RowMapper<Grocerysrecipe> rowMapper = (rs, rowNum) -> {
        Grocerysrecipe grocerysRecipe = new Grocerysrecipe();

        grocerysRecipe.setId(rs.getString("id"));
        grocerysRecipe.setQuantity(rs.getInt("quantity"));
        grocerysRecipe.setGroceryListId(rs.getString("grocery_list_id"));
        grocerysRecipe.setRecipeId(rs.getInt("recipe_id"));
        return grocerysRecipe;
    };

    public GrocerysRecipeJdbcDAO(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List list() {
        String sql = "SELECT * FROM grocerys_recipe";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void create(Object o) {

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
    public ResponseEntity<Object> addToNewGroceryList(HashMap<String, Object> info, String memberId) {

        String groceryListId = info.get("groceryListId").toString();
        int recipeId = Integer.valueOf(info.get("recipeId").toString());
        HashMap<String,Object> recipeObj = (HashMap<String, Object>) info.get("RecipeObj");

        // to use for Ok responses ------------------------------
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("grocerListId", groceryListId);
        responseBody.put("recipeId", recipeId);
        // ------------------------------------------------------

        Boolean found = searchGrocerysRecipes(groceryListId, recipeId, memberId);

        if (found == true)
        {
            incrementGrocerysRecipeCount(groceryListId, recipeId);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
        else {
            insertIntoRecipes(recipeId, recipeObj);

            String sql = "INSERT INTO grocerys_recipe (grocery_list_id, recipe_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

            int rows = jdbcTemplate.update(sql, UUID.fromString(groceryListId), recipeId);

            if (rows > 0) {
                return new ResponseEntity<>(responseBody, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("bad", HttpStatus.BAD_REQUEST);
            }
        }

    }


    public ResponseEntity<Object> addToExistingGroceryList(HashMap<String, Object> info, String memberId) {
        String listName = info.get("listName").toString();
        int recipeId = Integer.valueOf(info.get("recipeId").toString());
        HashMap<String,Object> recipeObj = (HashMap<String, Object>) info.get("RecipeObj");

        String groceryListId =  getGroceryListId(listName, memberId).toString();

        Boolean found = searchGrocerysRecipes(groceryListId, recipeId, memberId);

        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("groceryListId", groceryListId);
        responseBody.put("recipeId", recipeId);

        if (found == true) {
            incrementGrocerysRecipeCount(groceryListId, recipeId);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
        else {
            insertIntoRecipes(recipeId, recipeObj);
            String sql = "INSERT INTO grocerys_recipe (grocery_list_id, recipe_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            int rows = jdbcTemplate.update(sql, UUID.fromString(groceryListId), recipeId);

            if (rows > 0) {
                return new ResponseEntity<>(responseBody, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("bad", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private UUID getGroceryListId(String listName, String memberId) {
        System.out.println("executing getGroceryListId()");
        String sql = "SELECT DISTINCT id FROM grocery_list WHERE list_name = ? AND member_id = ?";
        UUID result = jdbcTemplate.queryForObject(sql, UUID.class, listName, UUID.fromString(memberId));
        System.out.println("Result of getGroceryListId(): " + result.toString());
        return result;
    }

    private void incrementGrocerysRecipeCount(String groceryListId, int recipeId) {
       String sql = "UPDATE grocerys_recipe SET quantity = quantity + 1 WHERE grocery_list_id = \'"
               + groceryListId + "\' AND recipe_id = " + recipeId;

       jdbcTemplate.update(sql);
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

    private Boolean searchGrocerysRecipes(String groceryListId, int recipeId, String memberId) {
        String sql = "SELECT gr.* FROM grocerys_recipe gr, grocery_list gl WHERE gr.grocery_list_id = \'" + groceryListId
                + "\' AND gr.recipe_id = " + recipeId + " AND gl.member_id = \'" + memberId + "\' AND gr.grocery_list_id = gl.id";

        Grocerysrecipe grocerysrecipe = null;

        try {
            grocerysrecipe = jdbcTemplate.queryForObject(sql, rowMapper);
            return true;
        }
        catch (EmptyResultDataAccessException ex) {
            return false;
        }

    }

/**
    public ResponseEntity<Object> getAllFromGroceryList(String groceryListID) {
        String sql = "SELECT gr.quantity, gl.member_id, r.id, r.info, gl.list_name FROM grocerys_recipe AS gr INNER "
        + "JOIN grocery_list AS gl ON gl.id = gr.grocery_list_id INNER JOIN member as m ON m.id = gl.member_id INNER "
        + "JOIN recipe AS r ON r.id = gr.recipe_id WHERE gr.grocery_list_id = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, UUID.fromString(groceryListID));

        for(Map<String, Object> row : rows) {
            System.out.println("data)");
            int value1 = Integer.valueOf(row.get("quantity").toString());
            String value2 = (String) row.get("member_id");
            int value3 = Integer.valueOf(row.get("id").toString());
            HashMap<String, Object> value4 = (HashMap<String, Object>) row.get("info");
            String value5 = row.get("list_name").toString();
            System.out.println(value1);
            System.out.println(value2);
            System.out.println(value3);
            System.out.println(value4);
            System.out.println(value5);
        }
        return new ResponseEntity<>("bad", HttpStatus.BAD_REQUEST);
    }
*/
    public ResponseEntity<Object> deleteFromGroceryList(String memberId, String groceryListId, int recipeId) {

        int quantity = searchGrocerysRecipeCount(groceryListId, recipeId, memberId);

        if (quantity > 1) {
            // decrement count && return something not null;
            decrementGrocerysRecipeCount(groceryListId, recipeId);

            return new ResponseEntity<>("deleted", HttpStatus.OK);
        } else {
            String sql = "DELETE FROM grocerys_recipe WHERE grocery_list_id = ? AND recipe_id = ?";
            int rows = jdbcTemplate.update(sql, UUID.fromString(groceryListId), recipeId);

            if (rows > 0) {
                return new ResponseEntity<>("deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private int searchGrocerysRecipeCount(String groceryListId, int recipeId, String memberId) {
      String sql = "SELECT gr.quantity FROM grocerys_recipe gr, grocery_list gl WHERE gr.grocery_list_id = ? AND gr.recipe_id = ? AND gl.member_id = ? AND gr.grocery_list_id = gl.id";
       int quantity = jdbcTemplate.queryForObject(sql, Integer.class, UUID.fromString(groceryListId), recipeId, UUID.fromString(memberId));
      System.out.println("quantity: " + quantity);
      return quantity;
    }

    private void decrementGrocerysRecipeCount(String groceryListId, int recipeId) {
        String sql = "UPDATE grocerys_recipe SET quantity = quantity - 1 WHERE grocery_list_id = ? AND recipe_id = ?";
        jdbcTemplate.update(sql, UUID.fromString(groceryListId), recipeId);
    }


    public ResponseEntity<Object> getRecipesAndListNames(String memberId) {
        String sql = "SELECT r.info, gr.quantity, gl.list_name, gl.created_at FROM "
                + "recipe r, grocery_list gl, grocerys_recipe gr WHERE r.id = gr.recipe_id "
        + "AND gl.id = gr.grocery_list_id AND gl.member_id = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, UUID.fromString(memberId));

        List<HashMap<String, Object>> responseBody = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        for(Map<String, Object> row : rows) {
            HashMap<String, Object> value1 =
            new Gson().fromJson(((PGobject) row.get("info")).getValue(), HashMap.class);

            int value2 = Integer.valueOf(row.get("quantity").toString());
            String value3 = row.get("list_name").toString();
            String value4 = row.get("created_at").toString();

            map.put("recipe", value1);
            map.put("quantity", value2);
            map.put("listName", value3);
            map.put("createdAt", value4);

            responseBody.add(map);
        }

        if (responseBody == null) {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
    }
}
