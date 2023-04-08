package com.example.mealprep;

import com.example.mealprep.config.JwtService;
import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Member;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {MemberTests.class})
public class MemberTests {
    @Mock
    JdbcTemplate jdbcTemplate;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthenticationManager authenticationManager;

    private JwtService jwtService = new JwtService();

    private JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();

    private MemberJdbcDAO memberJdbcDAO;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        memberJdbcDAO = new MemberJdbcDAO(jdbcTemplate, passwordEncoder, authenticationManager, jwtService, mailSenderImpl);
    }

    @Test
    @DisplayName("Successfully creates a new valid account.")
    @Order(1)
    public void test_createNewAccount()
    {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", "MockitoTest@gmail.com");
        info.put("username", "MockitoTest");
        info.put("password", "Zapata101");

       Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");
       member.setId(UUID.randomUUID().toString());

        MemberJdbcDAO memberJdbcDAOspy = spy(memberJdbcDAO);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class))).thenReturn(member);

        when(memberJdbcDAOspy.userWithUsernameExists(member.getUsername())).thenReturn(false);
        when(memberJdbcDAOspy.userWithEmailExists(member.getEmail())).thenReturn(false);


        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), memberJdbcDAOspy.create(member).getStatusCode());

    }

    @Test
    @Order(2)
    @DisplayName("Gives error when trying to create account with existing email.")
    public void test_attemptCreatingExistingEmailAccount()
    {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", "MockitoTest@gmail.com");
        info.put("username", "MockitoTest");
        info.put("password", "Zapata101");

        Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");
        member.setId(UUID.randomUUID().toString());

        MemberJdbcDAO memberJdbcDAOspy = spy(memberJdbcDAO);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class))).thenReturn(member);

        when(memberJdbcDAOspy.userWithUsernameExists(member.getUsername())).thenReturn(false);
        when(memberJdbcDAOspy.userWithEmailExists(member.getEmail())).thenReturn(true);


        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).build().getStatusCode(), memberJdbcDAOspy.create(member).getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("Gives error when trying to create account with existing Username.")
    public void test_attemptCreatingExistingUsernameAccount()
    {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", "MockitoTest@gmail.com");
        info.put("username", "MockitoTest");
        info.put("password", "Zapata101");

        Member member = new Member(info.get("email"), info.get("username"), info.get("password"), false, "native");
        member.setId(UUID.randomUUID().toString());

        MemberJdbcDAO memberJdbcDAO = spy(new MemberJdbcDAO(jdbcTemplate, passwordEncoder, authenticationManager, jwtService, mailSenderImpl));

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class))).thenReturn(member);

        when(memberJdbcDAO.userWithUsernameExists(member.getUsername())).thenReturn(true);
        when(memberJdbcDAO.userWithEmailExists(member.getEmail())).thenReturn(false);


        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).build().getStatusCode(), memberJdbcDAO.create(member).getStatusCode());
    }
}


