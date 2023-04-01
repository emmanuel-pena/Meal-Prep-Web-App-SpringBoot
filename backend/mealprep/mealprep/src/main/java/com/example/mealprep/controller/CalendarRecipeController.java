package com.example.mealprep.controller;
import com.example.mealprep.dao.CalendarRecipeJdbcDAO;
import com.example.mealprep.dao.DAO;
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

@RestController
public class CalendarRecipeController {

    @Autowired
    @Qualifier("CalendarRecipeJdbcDAO")
    private DAO<Calendarrecipe> crDao;
    private final CalendarRecipeJdbcDAO calendarRecipeJdbcDAO;

    @Autowired
    public CalendarRecipeController(CalendarRecipeJdbcDAO calendarRecipeJdbcDAO) {
        this.calendarRecipeJdbcDAO = calendarRecipeJdbcDAO;
    }

    @PostMapping("/calendarrecipe")
    public ResponseEntity<Object> addToMealCalendarTable(@RequestBody HashMap<String, Object> info) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return calendarRecipeJdbcDAO.addToMealCalendarTable(info, user.getId());
    }

    @GetMapping("/calendarrecipe")
    public ResponseEntity<Object> getFromMealCalendarTable() {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return calendarRecipeJdbcDAO.getAllFromMealCalendarTable(user.getId());
    }

    @DeleteMapping("/calendarrecipe")
    public ResponseEntity<Object> deleteFromMealCalendarTable(@RequestParam String date,
                                                              @RequestParam String mealId,
                                                              @RequestParam String mealType) {

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return calendarRecipeJdbcDAO.deleteFromMealCalendarTable(date, mealId, user.getId(), mealType);
    }

}
