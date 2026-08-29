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
-- Table structure for table `flow_edges`
--

DROP TABLE IF EXISTS `flow_edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flow_edges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `condition_expression` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `priority` int NOT NULL,
  `flow_id` bigint NOT NULL,
  `source_node_id` bigint NOT NULL,
  `target_node_id` bigint NOT NULL,
  `label` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_port` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_port` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj6bq47axudmwa5ram0m7lxoh7` (`public_id`),
  KEY `idx_edge_source` (`source_node_id`),
  KEY `idx_edge_target` (`target_node_id`),
  KEY `idx_edge_source_port` (`source_node_id`,`source_port`),
  KEY `idx_edge_flow_source_port` (`flow_id`,`source_node_id`,`source_port`),
  CONSTRAINT `FK2yvkmbtuakwt1ajxnn9lviep6` FOREIGN KEY (`source_node_id`) REFERENCES `flow_nodes` (`id`),
  CONSTRAINT `FKqyeop0u5x9rlauvqkai7m1va4` FOREIGN KEY (`flow_id`) REFERENCES `flows` (`id`),
  CONSTRAINT `FKs4w5d7go5uasctouydlca8iyy` FOREIGN KEY (`target_node_id`) REFERENCES `flow_nodes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_edges`
--

LOCK TABLES `flow_edges` WRITE;
/*!40000 ALTER TABLE `flow_edges` DISABLE KEYS */;
INSERT INTO `flow_edges` VALUES (1,'2026-08-21 14:00:07.405859',1,NULL,1,0,'e4fbf45d-c2bd-4f29-9336-bd472f6f8c79','2026-08-21 14:00:07.405859',NULL,NULL,0,1,1,3,NULL,'',''),(2,'2026-08-21 14:00:55.804177',1,NULL,1,0,'29fffa45-33de-4695-af4c-8ee421931940','2026-08-21 14:00:55.804177',NULL,NULL,0,1,3,2,NULL,'','');
/*!40000 ALTER TABLE `flow_edges` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 16:55:50
