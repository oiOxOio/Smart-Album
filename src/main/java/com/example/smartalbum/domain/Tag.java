package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Tag implements Serializable {
    private Integer id;

    private String name;

    private Integer imageId;

    private static final long serialVersionUID = 1L;
}