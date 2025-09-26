package com.silverbridge.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

		@Column(unique = true, nullable = false)
		private String phoneNumber;

		@Column(nullable = false)
    private String password;

    private String birth;
    private Boolean gender;
    private Boolean social;
    private String region;
    private String textsize;

    public User(String name, String phoneNumber, String password, String birth, Boolean gender, Boolean social, String region, String textsize) {
        this.name = name;
				this.phoneNumber = phoneNumber;
        this.password = password;
        this.birth = birth;
        this.gender = gender;
        this.social = social;
        this.region = region;
        this.textsize = textsize;
    }
}