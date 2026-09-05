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
-- Table structure for table `flows`
--

DROP TABLE IF EXISTS `flows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flows` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `flow_type` enum('BOTH','INBOUND','OUTBOUND') COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','ARCHIVED','DRAFT','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` int NOT NULL,
  `agent_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmpyu3fn63bvra6c6h1c0pllyy` (`public_id`),
  KEY `idx_flow_agent` (`agent_id`),
  KEY `idx_flow_status` (`status`),
  CONSTRAINT `FKd9lxfcmwla1o4rqxg59ymx880` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flows`
--

LOCK TABLES `flows` WRITE;
/*!40000 ALTER TABLE `flows` DISABLE KEYS */;
INSERT INTO `flows` VALUES (1,'2026-08-21 13:33:40.223386',1,NULL,1,0,'6dc49470-f91f-4522-b4d0-02597c361b93','2026-08-21 14:27:53.940797',1,'Test outbound flow for call session runtime testing','OUTBOUND','Test Outbound Banking Flow','ACTIVE',1,1),(2,'2026-08-26 17:38:28.772471',1,NULL,1,0,'0e5b549e-64a1-4acf-8189-e2837cdfab3b','2026-08-26 17:56:45.824432',1,'Test outbound voice agent flow','OUTBOUND','Voice STT LLM TTS Test','ACTIVE',1,1),(3,'2026-08-29 17:23:59.367645',1,NULL,1,0,'61f31a8b-805b-420f-ae1b-02595ffe2801','2026-08-31 15:36:30.612764',1,'Outbound AI voice flow for multilingual banking conversations using Sarvam STT, LLM and TTS.','OUTBOUND','Banking Voice Assistant Outbound Flow','ACTIVE',1,2),(4,'2026-09-03 15:16:27.156645',1,NULL,1,0,'3592c474-fa6d-48dc-8ddf-9ff2d20ba2f8','2026-09-03 15:16:27.156645',NULL,'Default draft flow for the agent.','BOTH','Main Flow','DRAFT',1,3),(5,'2026-09-03 15:32:17.253645',1,NULL,1,0,'ecf238c0-ec1d-4f19-bc2e-75486384d096','2026-09-03 15:32:17.253645',NULL,'Outbound AI voice flow for multilingual banking conversations using Sarvam STT, LLM and TTS.','OUTBOUND','Banking Voice Assistant Outbound Flow','DRAFT',1,3),(6,'2026-09-03 16:34:36.532524',1,NULL,1,0,'ef4ea02d-79ed-4781-9109-b6c69dda1616','2026-09-03 16:34:36.532524',NULL,'NEW','OUTBOUND','NEW','DRAFT',1,4);
/*!40000 ALTER TABLE `flows` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-05 10:15:20
