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
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT '1',
  `is_active` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `display_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_system` int NOT NULL,
  `is_visible` int NOT NULL,
  `route` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_modules_public_id` (`public_id`),
  UNIQUE KEY `uq_modules_code` (`module_code`),
  UNIQUE KEY `uq_modules_name` (`module_name`),
  KEY `ix_modules_active` (`is_active`),
  KEY `ix_modules_deleted` (`is_deleted`),
  KEY `ix_modules_code` (`module_code`),
  KEY `ix_modules_display_order` (`display_order`),
  KEY `ix_modules_module_code` (`module_code`),
  KEY `ix_modules_module_name` (`module_name`),
  KEY `ix_modules_is_active` (`is_active`),
  KEY `ix_modules_is_deleted` (`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modules`
--

LOCK TABLES `modules` WRITE;
/*!40000 ALTER TABLE `modules` DISABLE KEYS */;
INSERT INTO `modules` VALUES (1,'c8302716-915a-11f1-8f21-004238be78e0','CORE_PLATFORM','Core Platform','Core platform management',NULL,1,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(2,'c830402e-915a-11f1-8f21-004238be78e0','AI_AGENT','AI Agent','AI Agent Management',NULL,2,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(3,'c83042f8-915a-11f1-8f21-004238be78e0','FLOW','Workflow Builder','Workflow Builder',NULL,3,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(4,'c83044eb-915a-11f1-8f21-004238be78e0','KNOWLEDGE_BASE','Knowledge Base','Knowledge Base Management',NULL,4,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(5,'c8304725-915a-11f1-8f21-004238be78e0','CAMPAIGN','Campaign','Campaign Management',NULL,5,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(6,'c8304908-915a-11f1-8f21-004238be78e0','AI_DIALER','AI Dialer','AI Dialer Management',NULL,6,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(7,'c8304ad4-915a-11f1-8f21-004238be78e0','CONVERSATION','Conversation','Conversation Management',NULL,7,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(8,'c8304c9e-915a-11f1-8f21-004238be78e0','TELEPHONY','Telephony','Telephony Management',NULL,8,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(9,'c8304eaf-915a-11f1-8f21-004238be78e0','ANALYTICS','Analytics','Analytics & Reports',NULL,9,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(10,'c830507a-915a-11f1-8f21-004238be78e0','BILLING','Billing','Billing & Subscription',NULL,10,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL),(11,'c830524f-915a-11f1-8f21-004238be78e0','SYSTEM','System','System Configuration',NULL,11,1,'2026-08-06 11:20:51',1,NULL,NULL,0,NULL,'',0,0,NULL);
/*!40000 ALTER TABLE `modules` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 12:38:15
