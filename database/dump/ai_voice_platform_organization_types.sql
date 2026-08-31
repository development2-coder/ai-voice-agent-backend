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
-- Table structure for table `organization_types`
--

DROP TABLE IF EXISTS `organization_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organization_type_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organization_type_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT '1',
  `is_active` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_organization_types_public_id` (`public_id`),
  UNIQUE KEY `uq_organization_types_code` (`organization_type_code`),
  UNIQUE KEY `uq_organization_types_name` (`organization_type_name`),
  KEY `ix_organization_types_active` (`is_active`),
  KEY `ix_organization_types_deleted` (`is_deleted`),
  KEY `ix_organization_types_code` (`organization_type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization_types`
--

LOCK TABLES `organization_types` WRITE;
/*!40000 ALTER TABLE `organization_types` DISABLE KEYS */;
INSERT INTO `organization_types` VALUES (1,'61ec3f56-915a-11f1-8f21-004238be78e0','BANK','Bank','Banking Organization',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(2,'61ed155c-915a-11f1-8f21-004238be78e0','NBFC','NBFC','Non Banking Financial Company',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(3,'61ed44ae-915a-11f1-8f21-004238be78e0','HOSPITAL','Hospital','Healthcare',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(4,'61ed47af-915a-11f1-8f21-004238be78e0','INSURANCE','Insurance','Insurance Company',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(5,'61ed490e-915a-11f1-8f21-004238be78e0','BPO','BPO','Business Process Outsourcing',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(6,'61ed4a84-915a-11f1-8f21-004238be78e0','EDUCATION','Education','Educational Institute',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(7,'61ed4c62-915a-11f1-8f21-004238be78e0','RETAIL','Retail','Retail Company',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL),(8,'61ed4d8c-915a-11f1-8f21-004238be78e0','OTHER','Other','Other',1,1,'2026-08-06 11:18:00',1,NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `organization_types` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 12:38:16
