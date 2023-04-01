package com.example.mealprep;
import com.example.mealprep.dao.DAO;
import com.example.mealprep.model.Member;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class MealprepApplication {
	private static DAO<Member> memberDao;

	public MealprepApplication(DAO<Member> memberDao) {
		this.memberDao = memberDao;
	}

	public static void main(String[] args) {

		SpringApplication.run(MealprepApplication.class, args);

		System.out.println("\n All Members -----------------------------\n");
		List<Member> members = memberDao.list();
		members.forEach( (n) -> System.out.println(n.isActivated()));

		System.out.println("done\n");
	}

}
