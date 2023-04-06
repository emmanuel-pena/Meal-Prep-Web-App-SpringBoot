package com.example.mealprep.dao;
import com.example.mealprep.model.Grocerylist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

@Component
@Qualifier("GroceryListJdbcDAO")
public class GroceryListJdbcDAO implements DAO {
    private final JdbcTemplate jdbcTemplate;

    RowMapper<Grocerylist> rowMapper = (rs, rowNum) -> {
        Grocerylist groceryList = new Grocerylist();

        groceryList.setId(rs.getString("id"));
        groceryList.setMemberId(rs.getString("member_id"));
        groceryList.setListName(rs.getString("list_name"));
        groceryList.setCreatedAt(rs.getString("created_At"));
        return groceryList;
    };

    public GroceryListJdbcDAO(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List list() {
        String sql = "SELECT * FROM grocery_list";

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

    public ResponseEntity<Object> addGroceryList(HashMap<String, Object> info, String memberId) {

        LocalDateTime ldt = LocalDateTime.now();
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                .format(ldt);

        Object[] params = {info.get("listName"), date, UUID.fromString(memberId)};
        String sql = "INSERT INTO grocery_list (list_name, created_at, member_id) VALUES (?, ?::date, ?) ON CONFLICT DO NOTHING RETURNING *";
        List<Grocerylist> list = jdbcTemplate.query(sql, rowMapper, params);

        if (list.isEmpty() == false) {
            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("listName", info.get("listName"));
            responseBody.put("date", date);
            responseBody.put("memberId", memberId);
            responseBody.put("listId", list.get(0).getId());

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> getGroceryLists(String memberId) {

        String sql = "SELECT * FROM grocery_list WHERE member_id = ?";

        List<Grocerylist> list = jdbcTemplate.query(sql, rowMapper, UUID.fromString(memberId));

        if (list == null) {
            return new ResponseEntity<>("bad_request", HttpStatus.BAD_REQUEST);
        }

        HashMap<String, Object> map = new HashMap<>();

        List<Object> responseBody = new ArrayList<>();

        list.forEach( (item) -> {
            map.put("list_name", item.getListName());
            map.put("listId", item.getId());
            map.put("created_at", item.getCreatedAt());
            responseBody.add(map);
         }
        );

      return new ResponseEntity<>(responseBody, HttpStatus.OK);

    }

    public ResponseEntity deleteGroceryList(String listName, String memberId) {

        int rows = jdbcTemplate.update("DELETE FROM grocery_list WHERE list_name = ? AND member_id = ?", listName, UUID.fromString(memberId));

        if (rows > 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
