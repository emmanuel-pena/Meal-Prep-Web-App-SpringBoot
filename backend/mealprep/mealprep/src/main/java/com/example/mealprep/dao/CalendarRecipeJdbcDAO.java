package com.example.mealprep.dao;
import com.example.mealprep.model.Calendarrecipe;
import com.google.gson.Gson;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Optional;

@Component
@Qualifier("CalendarRecipeJdbcDAO")
public class CalendarRecipeJdbcDAO implements DAO{

    private JdbcTemplate jdbcTemplate;

    RowMapper<Calendarrecipe> rowMapper = (rs, rowNum) -> {
        Calendarrecipe calendarRecipe = new Calendarrecipe();

        calendarRecipe.setMeal(rs.getString("meal"));
        calendarRecipe.setMemberId(rs.getString("member_id"));
        calendarRecipe.setRecipeId(rs.getInt("recipe_id"));
        calendarRecipe.setTitle(rs.getString("title"));
        calendarRecipe.setPlanned(rs.getString("planned"));
        return calendarRecipe;
    };

    public CalendarRecipeJdbcDAO(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List list() {
        String sql = "SELECT * FROM calendarRecipes";

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

    public ResponseEntity<Object> addToMealCalendarTable(HashMap<String, Object> info, String memberId) {
      String mealType = info.get("mealType").toString();
      int recipeId = Integer.valueOf(info.get("recipeId").toString());
      HashMap<String,Object> recipeObj = (HashMap<String, Object>) info.get("RecipeObj");
      String recipeName = recipeObj.get("title").toString();

      String date = info.get("date").toString();
      String plannedDate = "";

        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date temp = inputFormat.parse(date);
            plannedDate = outputFormat.format(temp);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }

      insertIntoRecipes(recipeId, recipeObj);

      String sql = "INSERT INTO calendarRecipes(memberId, meal, title, recipeId, planned) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
      jdbcTemplate.update(sql, UUID.fromString(memberId), mealType, recipeName, recipeId, plannedDate);

      HashMap<String, Object> responseBody = new HashMap<>();
      responseBody.put("recipeId", recipeId);
      responseBody.put("memberId", memberId);
      return new ResponseEntity<>(responseBody, HttpStatus.OK);
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

    public ResponseEntity<Object> getAllFromMealCalendarTable(String memberId) {
       String sql = "SELECT DISTINCT r.info, mr.meal, mr.title, mr.planned FROM calendarRecipes mr, recipe r WHERE mr.recipeId = r.id AND mr.memberID = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, UUID.fromString(memberId));

        List<HashMap<String, Object>> responseBody = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();

        for(Map<String, Object> row : rows) {
            HashMap<String, Object> value1 =
                    new Gson().fromJson(((PGobject) row.get("info")).getValue(), HashMap.class);

            String value2 = row.get("meal").toString();
            String value3 = row.get("title").toString();
            String value4 = row.get("planned").toString();

            map.put("recipe", value1);
            map.put("meal", value2);
            map.put("title", value3);
            map.put("planned", value4);

            responseBody.add(map);
        }

        if (responseBody == null) {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> deleteFromMealCalendarTable(String date, String mealId, String memberId, String mealType) {
       return new ResponseEntity<>("unimplemented", HttpStatus.NOT_IMPLEMENTED);
    }
}
