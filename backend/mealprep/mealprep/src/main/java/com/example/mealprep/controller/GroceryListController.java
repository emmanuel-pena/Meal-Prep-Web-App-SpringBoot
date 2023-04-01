package com.example.mealprep.controller;
import com.example.mealprep.dao.DAO;
import com.example.mealprep.dao.FavoriteRecipeJdbcDAO;
import com.example.mealprep.dao.GroceryListJdbcDAO;
import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Calendarrecipe;
import com.example.mealprep.model.Favoriterecipe;
import com.example.mealprep.model.Grocerylist;
import com.example.mealprep.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GroceryListController {

    @Autowired
    @Qualifier("GroceryListJdbcDAO")
    private DAO<Grocerylist> glDao;
    private final GroceryListJdbcDAO groceryListJdbcDAO;

    @Autowired
    GroceryListController(GroceryListJdbcDAO groceryListJdbcDAO) {
        this.groceryListJdbcDAO = groceryListJdbcDAO;
    }


    @PostMapping("/grocerylists")
    public ResponseEntity<Object> addGroceryList(@RequestBody HashMap<String, Object> info) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return groceryListJdbcDAO.addGroceryList(info, user.getId());
    }

    @GetMapping("/grocerylists")
    public ResponseEntity<Object> getGroceryLists() {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return groceryListJdbcDAO.getGroceryLists(user.getId());
    }

    @DeleteMapping("/grocerylists")
    public ResponseEntity deleteGroceryList(@RequestParam String listName) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return groceryListJdbcDAO.deleteGroceryList(listName, user.getId());
    }
}
