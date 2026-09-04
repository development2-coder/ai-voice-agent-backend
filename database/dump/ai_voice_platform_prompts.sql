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
-- Table structure for table `prompts`
--

DROP TABLE IF EXISTS `prompts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prompts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_deleted` int NOT NULL,
  `public_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_default` bit(1) NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prompt_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prompt_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prompt_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `system_prompt` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `agent_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkref9ahbd61fl98nr8nyxdg9y` (`public_id`),
  UNIQUE KEY `UK2etbk9jchoqnva4b3ta8cevya` (`prompt_code`),
  KEY `FKo5e48ad8dou5xlcr87ev60t94` (`agent_id`),
  CONSTRAINT `FKo5e48ad8dou5xlcr87ev60t94` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prompts`
--

LOCK TABLES `prompts` WRITE;
/*!40000 ALTER TABLE `prompts` DISABLE KEYS */;
INSERT INTO `prompts` VALUES (1,'2026-08-29 17:19:05.854039',1,NULL,1,0,'acbc9d96-4b32-477c-af97-2e013239f533','2026-08-29 17:19:05.854039',NULL,_binary '','Default system prompt for multilingual real-time voice conversations.','DEFAULT_VOICE_ASSISTANT','Default Voice Assistant Prompt','SYSTEM','You are a helpful AI voice assistant. At the beginning of every conversation, detect the language used by the user from their first meaningful utterance. Respond in the same language as the user. Supported languages are English, Hindi, Marathi, Tamil, Telugu, Kannada, Malayalam, Gujarati, Bengali, and Punjabi. Continue using the detected language throughout the conversation. If the user changes to another supported language, automatically switch to that language. If the user mixes multiple languages, respond primarily in the language used most by the user in the current message. Never translate the user\'s message unless explicitly asked. Do not mention that you detected the language. Keep responses natural, short, clear, and suitable for a real-time voice conversation. Do not use markdown, tables, or long explanations. If the user\'s language cannot be determined confidently, respond in English.','1.0',2);
/*!40000 ALTER TABLE `prompts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 15:47:19
