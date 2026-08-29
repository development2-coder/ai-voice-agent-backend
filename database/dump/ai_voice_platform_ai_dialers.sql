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
-- Table structure for table `ai_dialers`
--

DROP TABLE IF EXISTS `ai_dialers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_dialers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `calls_per_minute` int NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `dialer_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_concurrent_calls` int NOT NULL,
  `max_retry_attempts` int NOT NULL,
  `paused_at` datetime(6) DEFAULT NULL,
  `retry_delay_seconds` int NOT NULL,
  `scheduled_end_at` datetime(6) DEFAULT NULL,
  `scheduled_start_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('COMPLETED','DRAFT','FAILED','PAUSED','RUNNING','SCHEDULED','STOPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_id` bigint NOT NULL,
  `campaign_id` bigint NOT NULL,
  `flow_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo491hv6pspp3toby0ryq7q89c` (`public_id`),
  KEY `ix_ai_dialers_campaign_id` (`campaign_id`),
  KEY `ix_ai_dialers_agent_id` (`agent_id`),
  KEY `ix_ai_dialers_flow_id` (`flow_id`),
  KEY `ix_ai_dialers_status` (`status`),
  KEY `ix_ai_dialers_is_active` (`is_active`),
  KEY `ix_ai_dialers_is_deleted` (`is_deleted`),
  CONSTRAINT `FKfcq7tfhnr0hgheiby1xr6wiff` FOREIGN KEY (`flow_id`) REFERENCES `flows` (`id`),
  CONSTRAINT `FKgttc8quao3f0gyhdfdweqnutb` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`),
  CONSTRAINT `FKkoyb4tptuij5ylok9v3tkkt4q` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_dialers`
--

LOCK TABLES `ai_dialers` WRITE;
/*!40000 ALTER TABLE `ai_dialers` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_dialers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 16:55:47
