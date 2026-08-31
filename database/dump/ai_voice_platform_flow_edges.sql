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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_edges`
--

LOCK TABLES `flow_edges` WRITE;
/*!40000 ALTER TABLE `flow_edges` DISABLE KEYS */;
INSERT INTO `flow_edges` VALUES (1,'2026-08-21 14:00:07.405859',1,NULL,1,0,'e4fbf45d-c2bd-4f29-9336-bd472f6f8c79','2026-08-21 14:00:07.405859',NULL,NULL,0,1,1,3,NULL,'',''),(2,'2026-08-21 14:00:55.804177',1,NULL,1,0,'29fffa45-33de-4695-af4c-8ee421931940','2026-08-21 14:00:55.804177',NULL,NULL,0,1,3,2,NULL,'',''),(3,'2026-08-26 17:52:50.439363',1,NULL,1,0,'f5ebd259-ce50-4a7f-a0dd-2d834ee4f915','2026-08-26 17:52:50.439363',NULL,NULL,1,2,4,5,'Start to STT','main','main'),(4,'2026-08-26 17:53:15.023025',1,NULL,1,0,'66a6311f-a87a-468f-b682-5a1e480b66d4','2026-08-26 17:53:15.023025',NULL,NULL,1,2,5,6,'STT to LLM','main','main'),(5,'2026-08-26 17:53:39.209607',1,NULL,1,0,'1358541c-a8b1-44f8-9d4c-46cac80e863a','2026-08-26 17:53:39.209607',NULL,NULL,1,2,6,7,'LLM to TTS','main','main'),(6,'2026-08-26 17:53:55.386710',1,NULL,1,0,'bfebf57b-d1c3-43ad-b7cb-33a3058a4005','2026-08-26 17:53:55.386710',NULL,NULL,1,2,7,8,'TTS to End','main','main'),(7,'2026-08-29 17:46:49.159934',1,NULL,1,0,'eb2a366b-06fa-4657-ad4b-0d2c12e8aecd','2026-08-29 17:46:49.159934',NULL,'',0,3,9,10,'Start to Greeting','main','main'),(8,'2026-08-29 17:48:29.812028',1,NULL,1,0,'19db2114-b6cb-41af-a0f1-cc7a15d78e0d','2026-08-29 17:48:29.812028',NULL,'',0,3,10,11,'Greeting to TTS','main','main'),(9,'2026-08-29 17:49:47.013789',1,NULL,1,0,'d11c9fbc-cc38-4765-8e5a-647fd88cac40','2026-08-29 17:49:47.015085',NULL,'',0,3,11,12,'Greeting TTS to Customer STT','main','main'),(10,'2026-08-29 17:51:12.333888',1,NULL,1,0,'89ab087e-5cb8-4caa-b07b-8c9c266e5b3a','2026-08-29 17:51:12.333888',NULL,'',0,3,12,13,'Customer STT to LLM','main','main'),(11,'2026-08-29 17:53:09.130370',1,NULL,1,0,'db4be489-0fa5-4952-8e74-acdf98158f12','2026-08-29 17:53:09.130370',NULL,'',0,3,13,14,'LLM to Response TTS','main','main'),(12,'2026-08-29 18:04:18.017037',1,NULL,1,0,'9db4e49b-dff3-4340-9925-2d6ab780746f','2026-08-29 18:04:18.018036',NULL,'',0,3,14,12,'Response TTS to Customer STT','main','main');
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

-- Dump completed on 2026-08-31 12:38:12
