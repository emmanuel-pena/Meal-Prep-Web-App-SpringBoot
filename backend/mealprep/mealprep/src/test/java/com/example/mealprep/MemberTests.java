package com.example.mealprep;

import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Member;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = {MemberTests.class})
public class MemberTests {

    @Mock
    JdbcTemplate jdbcTemplate;

    @InjectMocks
    MemberJdbcDAO memberJdbcDAO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_createNewAccount()
    {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", "MockitoTest@gmail.com");
        info.put("username", "MockitoTest");
        info.put("password", "Zapata101");


        Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");

        when(memberJdbcDAO.create(member)).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

    }

    @Test
    public void test_giveErrorCreatingExistingAccount()
    {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", "MockitoTest@gmail.com");
        info.put("username", "MockitoTest");
        info.put("password", "Zapata101");

        Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");

        when(memberJdbcDAO.create(member)).thenReturn(ResponseEntity.status(HttpStatus.OK).build());


    }
}
