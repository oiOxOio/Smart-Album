package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Mail implements Serializable {
    private Integer id;

    private String mailName;

    private String mailCode;

    private Date createDate;

    private static final long serialVersionUID = 1L;
}