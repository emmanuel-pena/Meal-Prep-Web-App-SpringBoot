package com.example.mealprep.dao;
import com.example.mealprep.config.JwtService;
import com.example.mealprep.model.Member;
import com.example.mealprep.token.ConfirmationToken;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.Optional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Qualifier("MemberJdbcDAO")
public class MemberJdbcDAO implements DAO<Member> {

    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final JavaMailSenderImpl mailSenderImpl;

    RowMapper<Member> rowMapper = (rs, rowNum) -> {
        Member member = new Member();

        member.setId(rs.getString("id"));
        member.setEmail(rs.getString("email"));
        member.setUsername(rs.getString("username"));
        member.setPasswordHash(rs.getString("password_hash"));
        member.setActivated(rs.getBoolean("is_activated"));
        member.setAccountType(rs.getString("account_type"));

        return member;
    };

    RowMapper<ConfirmationToken> tokenRowMapper = (rs, rowNum) -> {
        ConfirmationToken token = new ConfirmationToken();

        token.setId(UUID.fromString(rs.getString("id")));
        token.setConfirmedAt(rs.getString("confirmedAt"));
        token.setCreatedAt(rs.getString("createdAt"));
        token.setExpiresAt(rs.getString("expiresAt"));
        token.setToken(rs.getString("token"));
        token.setMemberId(UUID.fromString(rs.getString("memberId")));

        return token;
    };


    @Override
    public List<Member> list() {
        String sql = "SELECT * FROM Member";
// new BeanPropertyRowMapper<Member>(Member.class)
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    @Transactional
    public ResponseEntity create(Member member) {
        if (userExists(member)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String encodedPassword = this.passwordEncoder.encode(member.getPassword());

        String sql = "INSERT INTO Member(email, username, password_hash, is_Activated, account_type) Values(?, ?, ?, ?, ?::accounttype)";

        jdbcTemplate.update(sql, member.getEmail(), member.getUsername(), encodedPassword,
                false, member.getAccountType());

        String sql2 = "SELECT * FROM Member Where email = \'" + member.getEmail() + "\'";
        Member createdMember = jdbcTemplate.queryForObject(sql2, rowMapper);
        UUID createdMemberId = UUID.fromString(createdMember.getId());
        String tokenId = UUID.randomUUID().toString();

        ConfirmationToken token = new ConfirmationToken(createdMemberId,
                tokenId,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusMinutes(15).toString(),
                createdMemberId);

        jdbcTemplate.update("INSERT INTO confirmation_token(id, token, createdAt, expiresAt, memberId) VALUES(?, ?, ?, ?, ?)", token.getId(), token.getToken(), token.getCreatedAt(), token.getExpiresAt(), token.getMemberId());


        //send verification email now
        sendVerification(createdMember.getEmail(), createdMember.getUsername(), token.getToken());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Async
    public void sendVerification(String toEmail, String toUsername, String token){

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            mailSenderImpl.setJavaMailProperties(props);

            mailSenderImpl.setHost("smtp.gmail.com");
            mailSenderImpl.setPort(587);
            mailSenderImpl.setUsername("mealpreppinghelp@gmail.com");
            mailSenderImpl.setPassword("crtnxmmttmmpnmbk");

            try {
                MimeMessage mimeMessage = mailSenderImpl.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setText(buildVerificationEmail(toUsername, "http://localhost:3000/verify?token=" + token), true);
                helper.setTo(toEmail);
                helper.setSubject("Confirm your account");
                helper.setFrom("mealpreppinghelp@gmail.com");
                mailSenderImpl.send(mimeMessage);

            } catch (Exception e) {
                System.out.println(e);
                throw new IllegalStateException("failed to send email!");
            }

    };

    private String buildVerificationEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    @Transactional
    public ResponseEntity confirm(String token) {

        String sql = "SELECT * FROM confirmation_token WHERE token = \'" + token + "\'";

        ConfirmationToken confirmationToken = null;
        try {
            confirmationToken = jdbcTemplate.queryForObject(sql, tokenRowMapper);
        }
        catch (EmptyResultDataAccessException ex) {
            throw new IllegalStateException("Token you are trying to confirm is not found in database!");
        }


        if (confirmationToken.getConfirmedAt() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        LocalDateTime expiredAt = LocalDateTime.parse(confirmationToken.getExpiresAt());

        if (expiredAt.isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
          //return new ResponseEntity<>("Token expired. Please Try to sign in again and click \"RESEND VERIFICATION\".", HttpStatus.UNAUTHORIZED);
        }

        // update row in tables to confirmed
        jdbcTemplate.update("UPDATE confirmation_token SET confirmedAt = ? WHERE token = ?", LocalDateTime.now().toString(), token);
        jdbcTemplate.update("UPDATE Member SET is_activated = true WHERE id = ?", confirmationToken.getMemberId());

        return ResponseEntity.status(HttpStatus.OK).build();
        // return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    @Async
    public void resendVerification(Member member) {
        String tokenId = UUID.randomUUID().toString();

        ConfirmationToken token = new ConfirmationToken(
                UUID.fromString(member.getId()),
                tokenId,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusMinutes(15).toString(),
                UUID.fromString(member.getId())
        );

        jdbcTemplate.update("DELETE FROM confirmation_token WHERE memberId = ?", token.getMemberId());
        jdbcTemplate.update("INSERT INTO confirmation_token(id, token, createdAt, expiresAt, memberId) VALUES(?, ?, ?, ?, ?)", token.getId(), token.getToken(), token.getCreatedAt(), token.getExpiresAt(), token.getMemberId());

        //send verification email now
        sendVerification(member.getEmail(), member.getUsername(), token.getToken());
    }
    public boolean userExists(Member member) {
        if (userWithEmailExists(member.getEmail()) == false && userWithUsernameExists(member.getUsername()) == false) {
            return false;
        } else {
            return true;
        }
    }

    public boolean userWithEmailExists(String email)
    {
        String select = "SELECT * FROM member WHERE email = \'" + email + "\'";
        try {
            Member res = jdbcTemplate.queryForObject(select, rowMapper);
            return true;
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }

    public boolean userWithUsernameExists(String username)
    {
        String select = "SELECT * FROM member WHERE username = \'" + username + "\'";
        try {
            Member res = jdbcTemplate.queryForObject(select, rowMapper);
            return true;
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }

    public ResponseEntity<Object> authenticateUser(String email, String username, String password){

        String select = "SELECT * FROM member ";

        if (email != null && username != null) {
            select += "WHERE email = \'" + email + "\' AND username = \'" + username + "\'";
        } else if (email != null) {
            select += "WHERE email = \'" + email +"\'";
        } else {
            select += "WHERE username = \'" + username + "\'";
        }

        Member res = null;

        try {
             res = jdbcTemplate.queryForObject(select, rowMapper);

        } catch (EmptyResultDataAccessException ex) {
            return new ResponseEntity<>("No member with that email or username exists", HttpStatus.UNAUTHORIZED);
        }

        if (passwordEncoder.matches(password, res.getPassword()) == false)
        {
            return new ResponseEntity<>("Failed to authenticate user", HttpStatus.UNAUTHORIZED);
        }
        else if (res.isActivated() == false)
        {
            return new ResponseEntity<>("User is not activated", HttpStatus.FORBIDDEN);
        }
        else
        {
           authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(res.getEmail(), password));

           String jwtToken = jwtService.generateToken(res);

            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("id", res.getId());
            responseBody.put("email",res.getEmail());
            responseBody.put("username", res.getUsername());
            responseBody.put("accessToken", jwtToken);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }

    }

    private String decodeJwt(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }
    public ResponseEntity resetPassword(String jwtToken, String password) {

        String[] parts = jwtToken.split("\\.");

        String payload = decodeJwt(parts[1]);

        String email = payload.substring(payload.indexOf(":\"") + 2, payload.indexOf("\",\""));

        Member user = null;
        try {
            user = getUserByEmail(email);
        }
        catch (EmptyResultDataAccessException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        jdbcTemplate.update("UPDATE Member SET password_hash = ? WHERE email = ?", passwordEncoder.encode(password), email);

        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("email", user.getEmail());
        responseBody.put("username", user.getUsername());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
    public ResponseEntity sendResetPassword(String toEmail) {
        Member user = null;

        try {
            user = getUserByEmail(toEmail);
        }
        catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!user.getAccountType().matches("native")) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        mailSenderImpl.setJavaMailProperties(props);

        mailSenderImpl.setHost("smtp.gmail.com");
        mailSenderImpl.setPort(587);
        mailSenderImpl.setUsername("mealpreppinghelp@gmail.com");
        mailSenderImpl.setPassword("crtnxmmttmmpnmbk");

        String jwtToken = jwtService.generateToken(user);

        try {
            MimeMessage mimeMessage = mailSenderImpl.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(buildResetPasswordEmail(user.getUsername(), "http://localhost:3000/resetPassword?token=" + jwtToken), true);
            helper.setTo(toEmail);
            helper.setSubject("Reset your password");
            helper.setFrom("mealpreppinghelp@gmail.com");
            mailSenderImpl.send(mimeMessage);

        } catch (Exception e) {
            System.out.println(e);
            throw new IllegalStateException("failed to send email!");
        }

      return ResponseEntity.status(HttpStatus.OK).build();
    }

    private String buildResetPasswordEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Please click on the below link to reset your password: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Reset password</a> </p></blockquote>\n <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    @Override
    public Optional<Member> get(int id) {
        return Optional.empty();
    }

    public Member getUserByEmail(String email) {
       Member member = null;

        try {
            member = jdbcTemplate.queryForObject("SELECT * FROM Member WHERE email = \'" + email + "\'", rowMapper);
        } catch (EmptyResultDataAccessException e){
            System.out.println("User with that email not found in getUserByEmail()");
            return null;
        }

        return member;
    }

    @Override
    public void update(Member member, int id) {

    }

    @Override
    public void delete(int id) {

    }

    @Transactional
    public ResponseEntity<Object> googleAuth(String email, String name, String picture) {
        String select = "SELECT * FROM Member WHERE email = \'" + email +"\'";

        upsertGoogleUser(email);

        Member user = null;

        try {
            user = jdbcTemplate.queryForObject(select, rowMapper);
        }
        catch (EmptyResultDataAccessException e){
            System.out.println(e);
            return new ResponseEntity<>("Failed to queryForObject for google-login user", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String jwtToken = jwtService.generateToken(user);

        HashMap<String, String> responseBody = new HashMap<>();

        responseBody.put("name", name);
        responseBody.put("email", email);
        responseBody.put("id", user.getId());
        responseBody.put("accessToken", jwtToken);
        responseBody.put("picture", picture);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    public void upsertGoogleUser(String email) {
        Member res = null;
        String select = "SELECT * FROM Member WHERE email = \'" + email +"\'";

        try {
            res = jdbcTemplate.queryForObject(select, rowMapper);
            return;
        }
        catch (EmptyResultDataAccessException e) {
            jdbcTemplate.update("INSERT INTO Member(email, is_activated, account_type) VALUES (?, ?, ?::accounttype)", email, true, "google");
        }

    }

}
