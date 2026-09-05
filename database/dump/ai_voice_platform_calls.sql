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
-- Table structure for table `calls`
--

DROP TABLE IF EXISTS `calls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calls` (
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
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direction` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration_seconds` int DEFAULT NULL,
  `ended_at` datetime(6) DEFAULT NULL,
  `failure_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `from_number` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_call_id` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recording_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `to_number` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `campaign_contact_id` bigint DEFAULT NULL,
  `transcript_file_path` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transfer_destination` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transfer_requested` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkntuujntby7wkh32iad03b5t8` (`public_id`),
  UNIQUE KEY `uk_call_provider_call_id` (`provider_call_id`),
  KEY `FK4422n70yko5makme361d7arvx` (`campaign_contact_id`),
  CONSTRAINT `FK4422n70yko5makme361d7arvx` FOREIGN KEY (`campaign_contact_id`) REFERENCES `campaign_contacts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calls`
--

LOCK TABLES `calls` WRITE;
/*!40000 ALTER TABLE `calls` DISABLE KEYS */;
INSERT INTO `calls` VALUES (2,'2026-08-27 14:44:21.051930',1,NULL,1,0,'2bf35676-f8fe-404b-997a-97d7c6fa0ea5','2026-08-27 14:44:22.889410',NULL,NULL,'Direct Agent outbound call.','OUTBOUND',NULL,NULL,NULL,'9513886363','EXOTEL','c3b957d3054c35e5eba23210516d1a8r',NULL,'2026-08-27 14:44:21.389953','in-progress','8010853917',NULL,NULL,NULL,_binary '\0'),(3,'2026-08-27 15:21:56.766744',1,NULL,1,0,'0a8d1362-6ea7-47a2-9353-6b92cf426d12','2026-08-27 15:21:58.269635',NULL,NULL,'Direct Agent outbound call.','OUTBOUND',NULL,NULL,NULL,'9513886363','EXOTEL','277a0050319c4fa8fed7a9eb23bc1a8r',NULL,'2026-08-27 15:21:56.890969','in-progress','8010853917',NULL,NULL,NULL,_binary '\0'),(4,'2026-08-27 15:33:06.183514',1,NULL,1,0,'412ee783-13e2-4993-9455-d73dd1b5aae3','2026-08-27 15:33:16.650482',NULL,NULL,'Direct Agent outbound call.','OUTBOUND',NULL,NULL,NULL,'9513886363','EXOTEL','dd719ab9a9aaef0d441b36e633071a8r',NULL,'2026-08-27 15:33:14.764534','in-progress','8010853917',NULL,NULL,NULL,_binary '\0'),(7,'2026-09-04 15:57:23.562757',1,NULL,1,0,'d562a359-58ca-4b25-90a9-6020a56e3c35','2026-09-04 16:07:08.912808',NULL,NULL,'Phase 1 outbound call test','OUTBOUND',3,'2026-09-04 16:07:08.910808','Exotel outbound call failed. Your account is not yet KYC compliant. This is mandatory before making outbound calls.','+912048565979','EXOTEL',NULL,NULL,'2026-09-04 16:07:05.908028','FAILED','+918010853917',1,NULL,NULL,_binary '\0');
/*!40000 ALTER TABLE `calls` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-05 10:15:16
