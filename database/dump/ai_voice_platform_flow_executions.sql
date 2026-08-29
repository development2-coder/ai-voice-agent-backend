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
-- Table structure for table `flow_executions`
--

DROP TABLE IF EXISTS `flow_executions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flow_executions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `call_public_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `context_data` longtext COLLATE utf8mb4_unicode_ci,
  `conversation_public_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `current_node_id` bigint DEFAULT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','FAILED','RUNNING','TRANSFERRED','WAITING_FOR_AI','WAITING_FOR_API','WAITING_FOR_INPUT','WAITING_FOR_TIMER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `flow_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKht7c4bq70b7qwerqqxaxuk0w4` (`public_id`),
  KEY `idx_execution_flow` (`flow_id`),
  KEY `idx_execution_call` (`call_public_id`),
  KEY `idx_execution_status` (`status`),
  CONSTRAINT `FKi5iq37i8w01923q828gwj2mh0` FOREIGN KEY (`flow_id`) REFERENCES `flows` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_executions`
--

LOCK TABLES `flow_executions` WRITE;
/*!40000 ALTER TABLE `flow_executions` DISABLE KEYS */;
/*!40000 ALTER TABLE `flow_executions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 16:55:50
