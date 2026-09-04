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
-- Table structure for table `organizations`
--

DROP TABLE IF EXISTS `organizations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organizations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organization_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organization_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `legal_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organization_type_id` bigint NOT NULL,
  `organization_status_id` bigint NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remarks` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `currency` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_format` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `registration_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_identification_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `time_format` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `timezone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_organizations_public_id` (`public_id`),
  UNIQUE KEY `uq_organizations_code` (`organization_code`),
  UNIQUE KEY `uq_organizations_email` (`email`),
  KEY `ix_organizations_name` (`organization_name`),
  KEY `ix_organizations_type` (`organization_type_id`),
  KEY `ix_organizations_status` (`organization_status_id`),
  KEY `ix_organizations_active` (`is_active`),
  KEY `ix_organizations_deleted` (`is_deleted`),
  CONSTRAINT `fk_organizations_organization_statuses` FOREIGN KEY (`organization_status_id`) REFERENCES `organization_statuses` (`id`),
  CONSTRAINT `fk_organizations_organization_types` FOREIGN KEY (`organization_type_id`) REFERENCES `organization_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organizations`
--

LOCK TABLES `organizations` WRITE;
/*!40000 ALTER TABLE `organizations` DISABLE KEYS */;
INSERT INTO `organizations` VALUES (1,'f08f1bdb-92e9-11f1-8f21-004238be78e0','INF_DIGITAL','Infinitio Digital','Infinitio Digital',8,1,'superadmin@ai.com','8010853917',NULL,NULL,1,'2026-08-08 10:58:08',1,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `organizations` ENABLE KEYS */;
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
