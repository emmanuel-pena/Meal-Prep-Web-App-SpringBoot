package com.example.mealprep.controller;
import com.example.mealprep.dao.DAO;
import com.example.mealprep.dao.FavoriteRecipeJdbcDAO;
import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Calendarrecipe;
import com.example.mealprep.model.Favoriterecipe;
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
public class FavoriteRecipeController {

    @Autowired
    @Qualifier("FavoriteRecipeJdbcDAO")
    private DAO<Favoriterecipe> favDao;
    private final FavoriteRecipeJdbcDAO favoriteRecipeJdbcDAO;

    @Autowired
    FavoriteRecipeController(FavoriteRecipeJdbcDAO favoriteRecipeJdbcDAO) {
        this.favoriteRecipeJdbcDAO = favoriteRecipeJdbcDAO;
    }

    @PostMapping("/favoriterecipe")
    public ResponseEntity<Object> addToFavorites(@RequestBody HashMap<String, Object> info) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return favoriteRecipeJdbcDAO.addToFavorites(info, user.getId());
    }

    @GetMapping("/favoriterecipe")
    public ResponseEntity<Object> getAllFromFavorites() {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return favoriteRecipeJdbcDAO.getAllFromFavorites(user.getId());
    }

    @DeleteMapping("/favoriterecipe")
    public ResponseEntity<Object> deleteFromFavorites(@RequestParam String recipeId) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return favoriteRecipeJdbcDAO.deleteFromFavorites(user.getId(), recipeId);
    }

}
