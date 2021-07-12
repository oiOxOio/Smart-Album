/*
 Navicat Premium Data Transfer

 Source Server         : smartAlbum
 Source Server Type    : MySQL
 Source Server Version : 50644
 Source Host           : 5hp.cc:3306
 Source Schema         : smartalbum

 Target Server Type    : MySQL
 Target Server Version : 50644
 File Encoding         : 65001

 Date: 12/07/2021 11:04:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for depository
-- ----------------------------
DROP TABLE IF EXISTS `depository`;
CREATE TABLE `depository`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '仓库的名字',
  `size` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '当前仓库的大小，单位byte',
  `size_max` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '最大容量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `depository_name_uindex`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户注册后，自动生成一个文件夹，当做是用户的个人空间，即仓库' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for image
-- ----------------------------
DROP TABLE IF EXISTS `image`;
CREATE TABLE `image`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `depository_id` int(11) NULL DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '/',
  `url_mini` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `url` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `size` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `state_id` int(11) NULL DEFAULT 1,
  `image_set_id` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 723 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '放图片相关信息的' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for image_set
-- ----------------------------
DROP TABLE IF EXISTS `image_set`;
CREATE TABLE `image_set`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `depository_id` int(11) NULL DEFAULT NULL,
  `summary` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `detail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `background_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `wonderful_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `image_set_id_uindex`(`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 145 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for mail
-- ----------------------------
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mail_code` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for state
-- ----------------------------
DROP TABLE IF EXISTS `state`;
CREATE TABLE `state`  (
  `id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签名字',
  `image_id` int(11) NULL DEFAULT NULL COMMENT '图片的id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6771 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图像识别后，把识别的结果放在这张表里' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `mail` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `depository_id` int(11) NULL DEFAULT NULL COMMENT '用户仓库的id，也就是用户根目录，对应depository表',
  `register_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册的时间\n',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_username_uindex`(`username`) USING BTREE,
  UNIQUE INDEX `user_rootPath_uindex`(`depository_id`) USING BTREE,
  UNIQUE INDEX `user_mail_uindex`(`mail`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户信息' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
