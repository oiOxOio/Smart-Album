package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Depository implements Serializable {
    private Integer id;

    private String name;

    private String size;

    private String sizeMax;

    private static final long serialVersionUID = 1L;
}