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
-- Table structure for table `llm_interactions`
--

DROP TABLE IF EXISTS `llm_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `llm_interactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `call_public_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `final_response` bit(1) NOT NULL,
  `input_tokens` bigint DEFAULT NULL,
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latency_ms` bigint DEFAULT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `output_tokens` bigint DEFAULT NULL,
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_request_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_messages` longtext COLLATE utf8mb4_unicode_ci,
  `response_content` longtext COLLATE utf8mb4_unicode_ci,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_tokens` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeehm08mwjl3cbpjr85x527u3r` (`public_id`),
  KEY `idx_llm_interaction_call` (`call_public_id`),
  KEY `idx_llm_interaction_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `llm_interactions`
--

LOCK TABLES `llm_interactions` WRITE;
/*!40000 ALTER TABLE `llm_interactions` DISABLE KEYS */;
INSERT INTO `llm_interactions` VALUES (1,'2026-08-27 10:36:14.575329',1,NULL,1,0,'377c0529-7d20-4c5f-9be1-507ef56ed1d8','2026-08-27 10:36:14.575329',NULL,'test-call-db-001',NULL,_binary '',31,'mr-IN',5176,'sarvam-105b-conversations',84,'sarvam','20260827_e2fd620a-b067-4a1b-9a5d-ce8b4a4edd55','[{\"role\":\"user\",\"content\":\"नमस्कार, ही सरळम स्पीच टू टेक्स्ट ए बी आय जी चाचणी आहे.\"}]','\nनमस्कार! होय, ही सर्वम एआय (Sarvam AI) च्या स्पीच-टू-टेक्स्ट (Saaras v3) मॉडेलची चाचणी आहे. \n\nमी सर्वम एआयने तयार केलेला असिस्टंट आहे. तुम्हाला या चाचणीबद्दल काही विशिष्ट माहिती हवी आहे, किंवा इतर कोणत्याही विषयावर मदत हवी असल्यास कृपया सांगा. मी तुम्हाला कशी मदत करू?','SUCCESS',115);
/*!40000 ALTER TABLE `llm_interactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-05 10:15:19
