package com.example.mealprep.controller;
import com.example.mealprep.dao.*;
import com.example.mealprep.model.*;
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
public class GrocerysRecipeController
{
    @Autowired
    @Qualifier("GrocerysRecipeJdbcDAO")
    private DAO<Grocerysrecipe> grDao;

    private final GrocerysRecipeJdbcDAO grocerysRecipeJdbcDAO;

    @Autowired
    public GrocerysRecipeController(GrocerysRecipeJdbcDAO grocerysRecipeJdbcDAO) {
        this.grocerysRecipeJdbcDAO = grocerysRecipeJdbcDAO;
    }

    @PostMapping("/groceryrecipe")
    public ResponseEntity<Object> addToNewGroceryList(@RequestBody HashMap<String, Object> info) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return grocerysRecipeJdbcDAO.addToNewGroceryList(info, user.getId());
    }

    @PostMapping("/groceryrecipeexisting")
    public ResponseEntity<Object> addToExistingGroceryList(@RequestBody HashMap<String, Object> info) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return grocerysRecipeJdbcDAO.addToExistingGroceryList(info, user.getId());
    }

    /**
    @GetMapping("/groceryrecipe")
    public ResponseEntity<Object> getAllFromGroceryList(@RequestParam String groceryListID) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return grocerysRecipeJdbcDAO.getAllFromGroceryList(groceryListID);
    }
     */

    @DeleteMapping("/groceryrecipe")
    public ResponseEntity<Object> deleteFromGroceryList(@RequestParam String groceryListId, @RequestParam int recipeId) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return grocerysRecipeJdbcDAO.deleteFromGroceryList(user.getId(), groceryListId, recipeId);
    }

    @GetMapping("/recipesandlistnames")
    public ResponseEntity<Object> getRecipesAndListNames() {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return grocerysRecipeJdbcDAO.getRecipesAndListNames(user.getId());
    }
}
