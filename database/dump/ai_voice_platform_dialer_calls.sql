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
-- Table structure for table `dialer_calls`
--

DROP TABLE IF EXISTS `dialer_calls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dialer_calls` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `answered_at` datetime(6) DEFAULT NULL,
  `attempt_number` int NOT NULL,
  `duration_seconds` int DEFAULT NULL,
  `ended_at` datetime(6) DEFAULT NULL,
  `exotel_call_id` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `failure_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `flow_execution_public_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hangup_reason` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('ANSWERED','BUSY','CANCELLED','COMPLETED','DIALING','DND_BLOCKED','FAILED','IN_PROGRESS','NO_ANSWER','PENDING','QUEUED','REJECTED','RINGING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `campaign_contact_id` bigint NOT NULL,
  `dialer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfkav59o0dk5vxcre2popm0kw7` (`public_id`),
  KEY `ix_dialer_calls_dialer_id` (`dialer_id`),
  KEY `ix_dialer_calls_campaign_contact_id` (`campaign_contact_id`),
  KEY `ix_dialer_calls_status` (`status`),
  KEY `ix_dialer_calls_exotel_call_id` (`exotel_call_id`),
  KEY `ix_dialer_calls_scheduled_at` (`scheduled_at`),
  CONSTRAINT `FK8e4kfdjtuwpifk0bj2a96r3mj` FOREIGN KEY (`campaign_contact_id`) REFERENCES `campaign_contacts` (`id`),
  CONSTRAINT `FKne8ry03y3if307ufcv1n1vcye` FOREIGN KEY (`dialer_id`) REFERENCES `ai_dialers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dialer_calls`
--

LOCK TABLES `dialer_calls` WRITE;
/*!40000 ALTER TABLE `dialer_calls` DISABLE KEYS */;
/*!40000 ALTER TABLE `dialer_calls` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 12:18:04
