/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.3.66_3306
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : 192.168.3.66:3306
 Source Schema         : vertx

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 30/09/2020 10:19:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `kid` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`kid`) USING BTREE,
  UNIQUE INDEX `index_users`(`username`, `password`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (11, 'admin', '666666');
INSERT INTO `sys_user` VALUES (12, 'admin', '888888');
INSERT INTO `sys_user` VALUES (13, 'super', '666666');
INSERT INTO `sys_user` VALUES (14, 'txh', '000000');
INSERT INTO `sys_user` VALUES (15, 'txh', '000111');
INSERT INTO `sys_user` VALUES (16, 'tzz', '000000');

SET FOREIGN_KEY_CHECKS = 1;
