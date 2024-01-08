/*
 Navicat MySQL Data Transfer

 Source Server         : win
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : shixun_2

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 08/01/2024 14:08:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` int(0) NOT NULL AUTO_INCREMENT,
  `book_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `book_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `book_price` float(10, 2) NOT NULL,
  `book_publisher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `book_state` int(0) NOT NULL DEFAULT 0,
  `add_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`book_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book
-- ----------------------------
INSERT INTO `book` VALUES (24, '人体简史', '比尔.布莱森', 68.10, '人民出版社', 0, '2022-05-14');
INSERT INTO `book` VALUES (25, '人间快乐杂货铺', '丰子恺', 45.21, '北京邮电出版社', 0, '2021-12-05');
INSERT INTO `book` VALUES (26, '零基础围棋入门', '陈晓', 50.28, '人民出版社', 0, '2022-01-25');
INSERT INTO `book` VALUES (27, 'Spark实战', '张三', 68.45, '清华大学出版社', 0, '2022-04.26');
INSERT INTO `book` VALUES (28, 'Flink入门', '张三', 58.95, '清华大学出版社', 0, '2022-07-14');
INSERT INTO `book` VALUES (29, 'Java基础', '李四', 49.80, '重庆邮电大学出版社', 0, '2023-01-08');
INSERT INTO `book` VALUES (30, 'Bootstrap响应式网页开发', '章早立', 51.00, '北京邮电大学出版社', 0, '2024-01-01');
INSERT INTO `book` VALUES (32, '三国演义', '罗贯中', 49.51, '人民出版社', 0, '2024-01-04');
INSERT INTO `book` VALUES (33, '水浒传', '施耐庵', 54.25, '人民出版社', 0, '2024-01-04');
INSERT INTO `book` VALUES (69, '西游记', '吴承恩', 54.26, '人民出版社', 0, '2024-01-05');
INSERT INTO `book` VALUES (70, '红楼梦', '曹雪芹', 49.50, '人民出版社', 0, '2024-01-05');
INSERT INTO `book` VALUES (71, 'Vue应用程序开发', '张三', 56.84, '人民邮电出版社', 0, '2024-01-05');
INSERT INTO `book` VALUES (73, 'JSP编程技术', '吴越', 56.14, '清华大学出版社', 0, '2024-01-06');

-- ----------------------------
-- Table structure for borrowed
-- ----------------------------
DROP TABLE IF EXISTS `borrowed`;
CREATE TABLE `borrowed`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `userID` int(0) NOT NULL,
  `book_id` int(0) NOT NULL,
  `book_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `book_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `book_publisher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` int(0) NOT NULL,
  `oldStatus` int(0) NULL DEFAULT NULL,
  `borrow_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `repaid_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 140 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrowed
-- ----------------------------
INSERT INTO `borrowed` VALUES (146, 202208744, 26, '零基础围棋入门', '陈晓', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (147, 202208744, 25, '人间快乐杂货铺', '丰子恺', '北京邮电出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (148, 202208744, 24, '人体简史', '比尔.布莱森', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (149, 202208744, 32, '三国演义', '罗贯中', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (150, 202208744, 33, '水浒传', '施耐庵', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (151, 202208744, 69, '西游记', '吴承恩', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (152, 202208744, 70, '红楼梦', '曹雪芹', '人民出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (153, 202208722, 27, 'Spark实战', '张三', '清华大学出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (154, 202208722, 28, 'Flink入门', '张三', '清华大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (155, 202208722, 29, 'Java基础', '李四', '重庆邮电大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (156, 202208722, 30, 'Bootstrap响应式网页开发', '章早立', '北京邮电大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (157, 202208711, 25, '人间快乐杂货铺', '丰子恺', '北京邮电出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (158, 202208711, 24, '人体简史', '比尔.布莱森', '人民出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (159, 202208711, 26, '零基础围棋入门', '陈晓', '人民出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (160, 202208111, 27, 'Spark实战', '张三', '清华大学出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (161, 202208111, 28, 'Flink入门', '张三', '清华大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (162, 202208111, 29, 'Java基础', '李四', '重庆邮电大学出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (163, 202208111, 30, 'Bootstrap响应式网页开发', '章早立', '北京邮电大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (164, 202208111, 25, '人间快乐杂货铺', '丰子恺', '北京邮电出版社', 0, NULL, '2024-01-07', NULL);
INSERT INTO `borrowed` VALUES (165, 202208111, 73, 'JSP编程技术', '吴越', '清华大学出版社', 1, 0, '2024-01-07', '2024-01-07');
INSERT INTO `borrowed` VALUES (166, 202208111, 71, 'Vue应用程序开发', '张三', '人民邮电出版社', 0, NULL, '2024-01-07', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `userID` int(0) NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` int(0) NOT NULL,
  `login_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`userID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (0, '0', 0, '2024-01-07 22:08:21', '2024-01-07 22:06:14');
INSERT INTO `user` VALUES (202208111, '08111', 1, '2024-01-07 22:07:23', NULL);
INSERT INTO `user` VALUES (202208711, '123456', 1, '2024-01-07 21:47:15', '');
INSERT INTO `user` VALUES (202208722, '123456', 1, '', '');
INSERT INTO `user` VALUES (202208733, '123456', 1, '', NULL);
INSERT INTO `user` VALUES (202208744, '1', 1, '2024-01-07 21:47:01', '2024-01-07 21:36:58');

SET FOREIGN_KEY_CHECKS = 1;
