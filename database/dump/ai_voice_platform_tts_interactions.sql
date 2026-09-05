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
-- Table structure for table `tts_interactions`
--

DROP TABLE IF EXISTS `tts_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tts_interactions` (
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
  `audio_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `call_public_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_path` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `final_response` bit(1) NOT NULL,
  `input_characters` int DEFAULT NULL,
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latency_ms` bigint DEFAULT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_request_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `speaker` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `text` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfbn48690oix9plhy2s9xv7ptp` (`public_id`),
  KEY `idx_tts_interaction_call` (`call_public_id`),
  KEY `idx_tts_interaction_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tts_interactions`
--

LOCK TABLES `tts_interactions` WRITE;
/*!40000 ALTER TABLE `tts_interactions` DISABLE KEYS */;
INSERT INTO `tts_interactions` VALUES (1,'2026-08-27 10:36:18.872541',1,NULL,1,0,'7566bac6-32a5-40f5-9c07-4ebb1dc99d9e','2026-08-27 10:36:18.872541',NULL,NULL,'http://localhost:8080/tts-audio/tts_test-call-db-001_e3a1c035-c7ed-46d5-a95c-b3e49c54ce47.mp3','test-call-db-001','audio/mpeg',NULL,'tts_test-call-db-001_e3a1c035-c7ed-46d5-a95c-b3e49c54ce47.mp3',NULL,_binary '',268,'mr-IN',4069,'bulbul:v3','sarvam',NULL,'shubh','SUCCESS','नमस्कार! होय, ही सर्वम एआय (Sarvam AI) च्या स्पीच-टू-टेक्स्ट (Saaras v3) मॉडेलची चाचणी आहे. \n\nमी सर्वम एआयने तयार केलेला असिस्टंट आहे. तुम्हाला या चाचणीबद्दल काही विशिष्ट माहिती हवी आहे, किंवा इतर कोणत्याही विषयावर मदत हवी असल्यास कृपया सांगा. मी तुम्हाला कशी मदत करू?');
/*!40000 ALTER TABLE `tts_interactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-05 10:15:17
