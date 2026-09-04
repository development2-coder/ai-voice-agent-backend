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
-- Table structure for table `agent_configs`
--

DROP TABLE IF EXISTS `agent_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agent_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `greeting_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `language` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `llm_model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `llm_provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_tokens` int DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stt_model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stt_provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `system_prompt` text COLLATE utf8mb4_unicode_ci,
  `temperature` decimal(4,2) DEFAULT NULL,
  `tts_model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tts_provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `voice` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `agent_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_config_agent` (`agent_id`),
  UNIQUE KEY `UK5n7wnwy7gawmgjn0htq0qxhrw` (`public_id`),
  CONSTRAINT `FKkgalx36qav7qrxcddun9qq73i` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agent_configs`
--

LOCK TABLES `agent_configs` WRITE;
/*!40000 ALTER TABLE `agent_configs` DISABLE KEYS */;
INSERT INTO `agent_configs` VALUES (1,'2026-08-29 17:00:15.771750',1,NULL,1,0,'47eee268-48aa-48b3-8a65-8b3f14857aa0','2026-08-29 17:00:15.772706',NULL,'Hello! How can I help you today?','en-IN','sarvam-105b-conversations','sarvam',2000,'DRAFT','saaras:v4','sarvam','You are a helpful AI voice assistant. At the beginning of every conversation, detect the language used by the user from their first meaningful utterance. Respond in the same language as the user. Supported languages are English, Hindi, Marathi, Tamil, Telugu, Kannada, Malayalam, Gujarati, Bengali, and Punjabi. Continue using the detected language throughout the conversation. If the user changes to another supported language, automatically switch to that language. If the user mixes multiple languages, respond primarily in the language used most by the user in the current message. Never translate the user\'s message unless explicitly asked. Do not mention that you detected the language. Keep responses natural, short, clear, and suitable for a real-time voice conversation. Do not use markdown, tables, or long explanations. If the user\'s language cannot be determined confidently, respond in English.',0.20,'bulbul:v3','sarvam','shubh',2);
/*!40000 ALTER TABLE `agent_configs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 15:47:20
