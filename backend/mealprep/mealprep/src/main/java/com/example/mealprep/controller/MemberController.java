package com.example.mealprep.controller;
import com.example.mealprep.dao.DAO;
import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Calendarrecipe;
import com.example.mealprep.model.Favoriterecipe;
import com.example.mealprep.model.Member;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
public class MemberController {
  @Autowired
  @Qualifier("MemberJdbcDAO")
  private DAO<Member> memDao;
  private final MemberJdbcDAO memberJdbcDAO;

  @Autowired
  public MemberController(MemberJdbcDAO memberJdbcDAO) {
    this.memberJdbcDAO = memberJdbcDAO;
  }
  // Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

  @PostMapping("/verify")
  public ResponseEntity confirmToken(@RequestBody HashMap<String, String> info) {
    return memberJdbcDAO.confirm(info.get("token"));
  }


  @PostMapping("/resend_verification")
  public ResponseEntity<String> resendVerification(@RequestBody HashMap<String, String> info) {
    if ( !info.containsKey("email") ) {
      return new ResponseEntity<>("Bad request. Fields missing", HttpStatus.BAD_REQUEST);
    }

    Member member = memberJdbcDAO.getUserByEmail(info.get("email"));

    if (member == null) {
      return new ResponseEntity<>("User with that email does not exist in database", HttpStatus.BAD_REQUEST);
    }
    else {
      memberJdbcDAO.resendVerification(member);
      return new ResponseEntity<>(info.get("email"), HttpStatus.CREATED);
    }

  }

  @PostMapping("/forgotPassword")
  public ResponseEntity sendResetPassword(@RequestBody HashMap<String, String> info) {
    System.out.println(info.get("email"));
    return memberJdbcDAO.sendResetPassword(info.get("email"));
  }

  @PostMapping("/resetPassword")
  public ResponseEntity<Object> resetPassword(@RequestBody HashMap<String, String> info) {
    return memberJdbcDAO.resetPassword(info.get("token"), info.get("password"));
  }

  @PostMapping("/user")
  public ResponseEntity createMember(@RequestBody HashMap<String, String> info) {
    if (!info.containsKey("email") || !info.containsKey("username") || !info.containsKey("password")) {
      return new ResponseEntity<>("Bad request. Fields missing", HttpStatus.BAD_REQUEST);
    }

    Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");
    return memDao.create(member);
  }

  @PostMapping("/login")
  public ResponseEntity<Object> authenticateUser(@RequestBody HashMap<String, String> info) {
    if ((!info.containsKey("email") && !info.containsKey("username")) || !info.containsKey("password")) {
      return new ResponseEntity<>("Bad request. Fields missing", HttpStatus.BAD_REQUEST);
    }

    return memberJdbcDAO.authenticateUser(info.get("email"), info.get("username"), info.get("password"));

  }

  @PostMapping("/google-login")
  public ResponseEntity<Object> googleAuth(@RequestBody HashMap<String, String> info) {
    if ( !info.containsKey("email") || !info.containsKey("name") || !info.containsKey("picture")) {
      return new ResponseEntity<>("Bad request. Fields missing", HttpStatus.BAD_REQUEST);
    }

    return memberJdbcDAO.googleAuth(info.get("email"), info.get("name"), info.get("picture"));

  }


}
