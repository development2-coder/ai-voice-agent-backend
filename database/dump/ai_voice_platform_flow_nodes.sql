-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: ai_voice_platform
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flow_nodes`
--

DROP TABLE IF EXISTS `flow_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flow_nodes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `configuration` text COLLATE utf8mb4_unicode_ci,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `node_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `node_type` enum('AI_RESPONSE','API','CONDITION','END','FUNCTION','GREETING','KNOWLEDGE_BASE','MESSAGE','RAG','SET_VARIABLE','START','TRANSFER','USER_INPUT','WAIT','WEBHOOK') COLLATE utf8mb4_unicode_ci NOT NULL,
  `position_x` double DEFAULT NULL,
  `position_y` double DEFAULT NULL,
  `flow_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_node_key` (`flow_id`,`node_key`),
  UNIQUE KEY `UKguu3aqygc6eqg2mwo9neyfweu` (`public_id`),
  KEY `idx_flow_node_flow` (`flow_id`),
  CONSTRAINT `FKqoyp5p3t73c8arxhx1r12ugj3` FOREIGN KEY (`flow_id`) REFERENCES `flows` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_nodes`
--

LOCK TABLES `flow_nodes` WRITE;
/*!40000 ALTER TABLE `flow_nodes` DISABLE KEYS */;
INSERT INTO `flow_nodes` VALUES (1,'2026-08-21 13:51:02.270412',1,NULL,1,0,'d56273e6-2509-43fd-923c-97e2d93300c2','2026-08-21 13:51:02.270412',NULL,'{}','Start','start','START',100,100,1),(2,'2026-08-21 13:52:01.951271',1,NULL,1,0,'4de5cd52-a7a5-41df-b3e0-5d80f773c9ad','2026-08-21 13:52:01.951271',NULL,'{}','End','end','END',500,100,1),(3,'2026-08-21 13:56:09.420695',1,NULL,1,0,'a6d40a2f-c356-4639-82ab-6e8499592653','2026-08-21 13:56:09.420695',NULL,'{\"message\":\"नमस्कार! मी तुमची मदत करण्यासाठी येथे आहे.\"}','Welcome Message','welcome_message','MESSAGE',300,100,1);
/*!40000 ALTER TABLE `flow_nodes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 16:55:48
