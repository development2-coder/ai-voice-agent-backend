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
  `node_type` enum('AI_RESPONSE','API','CONDITION','END','FUNCTION','GREETING','KNOWLEDGE_BASE','LLM','MESSAGE','RAG','SET_VARIABLE','START','STT','TRANSFER','TTS','USER_INPUT','WAIT','WEBHOOK') COLLATE utf8mb4_unicode_ci NOT NULL,
  `position_x` double DEFAULT NULL,
  `position_y` double DEFAULT NULL,
  `flow_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_node_key` (`flow_id`,`node_key`),
  UNIQUE KEY `UKguu3aqygc6eqg2mwo9neyfweu` (`public_id`),
  KEY `idx_flow_node_flow` (`flow_id`),
  CONSTRAINT `FKqoyp5p3t73c8arxhx1r12ugj3` FOREIGN KEY (`flow_id`) REFERENCES `flows` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_nodes`
--

LOCK TABLES `flow_nodes` WRITE;
/*!40000 ALTER TABLE `flow_nodes` DISABLE KEYS */;
INSERT INTO `flow_nodes` VALUES (1,'2026-08-21 13:51:02.270412',1,NULL,1,0,'d56273e6-2509-43fd-923c-97e2d93300c2','2026-08-21 13:51:02.270412',NULL,'{}','Start','start','START',100,100,1),(2,'2026-08-21 13:52:01.951271',1,NULL,1,0,'4de5cd52-a7a5-41df-b3e0-5d80f773c9ad','2026-08-21 13:52:01.951271',NULL,'{}','End','end','END',500,100,1),(3,'2026-08-21 13:56:09.420695',1,NULL,1,0,'a6d40a2f-c356-4639-82ab-6e8499592653','2026-08-21 13:56:09.420695',NULL,'{\"message\":\"नमस्कार! मी तुमची मदत करण्यासाठी येथे आहे.\"}','Welcome Message','welcome_message','MESSAGE',300,100,1),(4,'2026-08-26 17:48:49.308943',1,NULL,1,0,'d9c6f413-190c-4233-9769-558c3e6d5123','2026-08-26 17:48:49.308943',NULL,'{}','Start','start','START',100,300,2),(5,'2026-08-26 17:49:26.652660',1,NULL,1,0,'208d0e80-ff58-441f-b98b-53ed48786896','2026-08-26 17:49:26.652660',NULL,'{\"language\":\"en-IN\",\"finalTranscript\":true}','Speech to Text','stt','STT',350,300,2),(6,'2026-08-26 17:49:46.470172',1,NULL,1,0,'4312901a-cef7-4762-8152-6c9dc9fc7406','2026-08-26 17:49:46.470172',NULL,'{\"language\":\"en-IN\",\"finalResponse\":true}','LLM','llm','LLM',600,300,2),(7,'2026-08-26 17:50:09.122745',1,NULL,1,0,'5e8cb6e7-f702-4162-a235-a1531c001178','2026-08-26 17:50:09.122745',NULL,'{\"language\":\"en-IN\",\"speaker\":\"shubh\",\"pace\":1.0,\"speechSampleRate\":22050,\"finalResponse\":true}','Text to Speech','tts','TTS',850,300,2),(8,'2026-08-26 17:50:29.066301',1,NULL,1,0,'e6406e0e-6578-4d6b-a977-e3357668a227','2026-08-26 17:50:29.066301',NULL,'{}','End','end','END',1100,300,2),(9,'2026-08-29 17:33:04.191532',1,NULL,1,0,'9c958622-aab0-4431-ae20-b817833c33c2','2026-08-29 17:33:04.191532',NULL,'{}','Start','start','START',100,300,3),(10,'2026-08-29 17:35:44.245592',1,NULL,1,0,'cdf9c6ca-d79a-46bc-9428-d438c68d9612','2026-08-29 17:35:44.245592',NULL,'{\"message\":\"Hello! This is your banking assistant. How can I help you today?\"}','Welcome Greeting','greeting','GREETING',300,300,3),(11,'2026-08-29 17:37:19.579170',1,NULL,1,0,'1e0c1306-c3c7-47a9-a368-da4d0da7d745','2026-08-29 17:37:19.579170',NULL,'{\"language\":\"en-IN\",\"speaker\":\"shubh\",\"pace\":1.0,\"speechSampleRate\":22050,\"finalResponse\":true}','Greeting Text to Speech','greeting_tts','TTS',500,300,3),(12,'2026-08-29 17:38:45.847469',1,NULL,1,0,'c3644207-a997-40ec-b7cc-805a0c572557','2026-08-29 17:38:45.847469',NULL,'{\"language\":\"auto\",\"finalTranscript\":true}','Customer Speech to Text','customer_stt','STT',700,300,3),(13,'2026-08-29 17:39:49.508552',1,NULL,1,0,'8fd712a3-29bd-485f-b08f-c7d4c55324b5','2026-08-29 17:39:49.508552',NULL,'{\"language\":\"auto\",\"finalResponse\":true}','Conversation LLM','conversation_llm','LLM',900,300,3),(14,'2026-08-29 17:40:53.818626',1,NULL,1,0,'2b0383ff-0c08-4b59-a6ca-8dd941b6a997','2026-08-29 17:40:53.818626',NULL,'{\"language\":\"auto\",\"speaker\":\"shubh\",\"pace\":1.0,\"speechSampleRate\":22050,\"finalResponse\":true}','AI Response Text to Speech','response_tts','TTS',1100,300,3),(15,'2026-08-31 15:29:30.719814',1,NULL,1,0,'5a7ad33f-f38d-401a-88a2-f66d1e9d19d8','2026-08-31 15:29:30.719814',NULL,'{}','End','end','END',1300,450,3);
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

-- Dump completed on 2026-08-31 15:47:15
