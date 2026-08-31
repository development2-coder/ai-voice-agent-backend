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
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tenant_id` bigint NOT NULL,
  `organization_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `password_changed_at` datetime DEFAULT NULL,
  `failed_login_attempts` int NOT NULL DEFAULT '0',
  `account_locked` tinyint(1) NOT NULL DEFAULT '0',
  `account_locked_until` datetime DEFAULT NULL,
  `is_email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `is_mobile_verified` tinyint(1) NOT NULL DEFAULT '0',
  `is_active` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `department` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `designation` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_verified` bit(1) DEFAULT NULL,
  `full_name` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `middle_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile_verified` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_public_id` (`public_id`),
  UNIQUE KEY `uq_users_username` (`username`),
  UNIQUE KEY `uq_users_email` (`email`),
  KEY `ix_users_username` (`username`),
  KEY `ix_users_email` (`email`),
  KEY `ix_users_mobile` (`mobile_number`),
  KEY `ix_users_role` (`role_id`),
  KEY `ix_users_organization` (`organization_id`),
  KEY `ix_users_tenant` (`tenant_id`),
  KEY `ix_users_active` (`is_active`),
  KEY `ix_users_deleted` (`is_deleted`),
  CONSTRAINT `fk_users_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_users_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'b0e19258-83d2-4a19-a936-738a5e3e7130',1,1,1,'superadmin','development2@infinitiodigital.com','$2a$10$XPEhxnLPvU7eVl2JK.WTB.xzQjieN5puN9NIvvRXphcstC6TV5KD6','Super','Admin','8010853917','',NULL,'2026-08-26 13:38:18',0,0,NULL,0,0,1,'2026-08-12 18:23:15',0,'2026-08-26 13:38:18',NULL,0,NULL,'Administration','System Administrator',_binary '\0','Super Admin','',_binary '\0');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 12:38:13
