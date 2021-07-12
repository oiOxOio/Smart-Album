package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {
    private Integer id;

    private String username;

    private String password;

    private String mail;

    private Integer depositoryId;

    private Date registerDate;

    private Depository depository;

    private static final long serialVersionUID = 1L;
}