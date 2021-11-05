package com.learningbybuilding.supportportal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

//do we need Serializable???
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String profileImageUrl;

    private Date lastLoginDate;
    private Date lastLoginDateDisplay;

    private Date joinDate;

    //security related
    private String role;
    private String[] authorities;
    private boolean isActive;
    private boolean isNotLocked;
}
