package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * image
 *
 * @author Administrator
 */
@Data
public class Image implements Serializable {
    private Integer id;

    private String name;

    private Integer depositoryId;

    private String path;

    private String urlMini;

    private String url;

    private String size;

    private Date createDate;

    private Date updateDate;

    private Integer stateId;

    private Integer imageSetId;

    private List<Tag> tags;

    private static final long serialVersionUID = 1L;
}