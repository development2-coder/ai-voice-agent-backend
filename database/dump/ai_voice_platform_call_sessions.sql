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
-- Table structure for table `call_sessions`
--

DROP TABLE IF EXISTS `call_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `call_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `agent_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_version` int NOT NULL,
  `call_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collected_slots` longtext COLLATE utf8mb4_unicode_ci,
  `conversation_history` longtext COLLATE utf8mb4_unicode_ci,
  `flow_execution_public_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `flow_node_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `language` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','ENDED','TRANSFERRING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `tenant_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `turn_index` int NOT NULL,
  `conversation_storage_key` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_call_session_call_id` (`call_id`),
  UNIQUE KEY `UKbfer2c5k243vquaswn785nkwh` (`public_id`),
  KEY `idx_call_session_tenant` (`tenant_id`),
  KEY `idx_call_session_agent` (`agent_id`),
  KEY `idx_call_session_status` (`status`),
  KEY `idx_call_session_deleted` (`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `call_sessions`
--

LOCK TABLES `call_sessions` WRITE;
/*!40000 ALTER TABLE `call_sessions` DISABLE KEYS */;
INSERT INTO `call_sessions` VALUES (1,'2026-08-21 15:38:30.749107',1,NULL,1,0,'4bfb9df8-1e0f-498e-a60c-4f62efa6bdf5','2026-08-21 16:08:22.967005',NULL,'70cef447-a1bf-4e48-956b-7743813acc5c',1,'test-call-003','{\"customerName\":\"Kiran\"}','[{\"role\":\"USER\",\"text\":\"नमस्कार\",\"timestamp\":\"2026-08-21T10:29:17.323417100Z\"},{\"role\":\"ASSISTANT\",\"text\":\"नमस्कार! मी तुम्हाला कशी मदत करू शकतो?\",\"timestamp\":\"2026-08-21T10:30:59.090623300Z\"}]',NULL,'a6d40a2f-c356-4639-82ab-6e8499592653','mr-IN','ENDED','9977224e-92ea-11f1-8f21-004238be78e0',2,NULL),(2,'2026-08-22 10:18:37.738185',1,NULL,1,0,'5fe4e46f-936f-4ac1-8e47-5e98cc807a2c','2026-08-22 13:17:36.635088',NULL,'70cef447-a1bf-4e48-956b-7743813acc5c',1,'test-call-004','{\"customerName\":\"Kiran\"}',NULL,NULL,'a6d40a2f-c356-4639-82ab-6e8499592653','mr-IN','ACTIVE','9977224e-92ea-11f1-8f21-004238be78e0',0,NULL);
/*!40000 ALTER TABLE `call_sessions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 12:18:03
