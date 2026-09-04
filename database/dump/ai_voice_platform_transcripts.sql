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
-- Table structure for table `transcripts`
--

DROP TABLE IF EXISTS `transcripts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transcripts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `ended_at` datetime(6) DEFAULT NULL,
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sequence_number` int NOT NULL,
  `source` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `speaker_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `text` varchar(5000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `call_recording_id` bigint NOT NULL,
  `call_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transcript_recording_sequence` (`call_recording_id`,`sequence_number`),
  UNIQUE KEY `UKqm0ufqa2hjh624wx7vf9gpurt` (`public_id`),
  UNIQUE KEY `uk_transcript_call_sequence` (`call_id`,`sequence_number`),
  KEY `idx_transcript_call` (`call_id`),
  KEY `idx_transcript_call_sequence` (`call_id`,`sequence_number`),
  KEY `idx_transcript_speaker_type` (`speaker_type`),
  CONSTRAINT `FKced3xqxg7rcj1gt9axo7oamji` FOREIGN KEY (`call_recording_id`) REFERENCES `call_recordings` (`id`),
  CONSTRAINT `FKchgjk7dokk2xv9yeyau2yo80e` FOREIGN KEY (`call_id`) REFERENCES `calls` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transcripts`
--

LOCK TABLES `transcripts` WRITE;
/*!40000 ALTER TABLE `transcripts` DISABLE KEYS */;
/*!40000 ALTER TABLE `transcripts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 15:47:19
