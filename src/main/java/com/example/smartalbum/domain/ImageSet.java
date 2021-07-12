package com.example.smartalbum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * image_set
 *
 * @author
 */
@Data
public class ImageSet implements Serializable {
    private Integer id;

    private String name;

    private Integer depositoryId;

    private String summary;

    private String detail;

    private String backgroundUrl;

    private String wonderfulUrl;

    private Date createTime;

    private Date updateTime;

    private List<Image> images;

    private static final long serialVersionUID = 1L;
}