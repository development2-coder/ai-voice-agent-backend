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
-- Table structure for table `stt_interactions`
--

DROP TABLE IF EXISTS `stt_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stt_interactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `audio_size_bytes` bigint DEFAULT NULL,
  `call_public_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `final_transcript` bit(1) NOT NULL,
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `language_probability` double DEFAULT NULL,
  `latency_ms` bigint DEFAULT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `transcript` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKku70vfmp3102gh7vand0v0ume` (`public_id`),
  KEY `idx_stt_interaction_call` (`call_public_id`),
  KEY `idx_stt_interaction_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stt_interactions`
--

LOCK TABLES `stt_interactions` WRITE;
/*!40000 ALTER TABLE `stt_interactions` DISABLE KEYS */;
INSERT INTO `stt_interactions` VALUES (3,'2026-08-27 10:36:09.222571',1,NULL,1,0,'bf776fb6-2337-44a4-a588-bda8d7527a88','2026-08-27 10:36:09.222571',NULL,165398,'test-call-db-001',NULL,_binary '','mr-IN',NULL,981,'saaras:v4','sarvam','SUCCESS','नमस्कार, ही सरळम स्पीच टू टेक्स्ट ए बी आय जी चाचणी आहे.');
/*!40000 ALTER TABLE `stt_interactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 15:47:16
