-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: is1_bv140094
-- ------------------------------------------------------
-- Server version	8.0.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alarm`
--

DROP TABLE IF EXISTS `alarm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alarm` (
  `Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Due` datetime DEFAULT NULL,
  `RepeatIntervalSeconds` int(11) DEFAULT NULL,
  `Sound` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alarm`
--

LOCK TABLES `alarm` WRITE;
/*!40000 ALTER TABLE `alarm` DISABLE KEYS */;
INSERT INTO `alarm` (`Id`, `Due`, `RepeatIntervalSeconds`, `Sound`) VALUES (1,'2019-06-07 20:00:01',60,'alarm2.wav'),(2,'2019-06-07 12:34:56',10800,'alarm3.wav'),(3,'2019-06-06 22:52:05',5,'alarm1.wav'),(4,'2019-06-07 17:00:01',86400,'alarm1.wav'),(5,'2019-06-16 00:00:00',0,'alarm2.wav');
/*!40000 ALTER TABLE `alarm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playback`
--

DROP TABLE IF EXISTS `playback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playback` (
  `Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `SearchTerm` varchar(255) DEFAULT NULL,
  `Title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playback`
--

LOCK TABLES `playback` WRITE;
/*!40000 ALTER TABLE `playback` DISABLE KEYS */;
INSERT INTO `playback` (`Id`, `SearchTerm`, `Title`) VALUES (1,'spektre gates of dawn','Spektre - Gates of Dawn (Original Mix)'),(2,'clutch the regulator','Clutch - The Regulator Lyrics'),(3,'the sound defects','The Sound Defects - The Iron Horse [Full album]'),(4,'mozart piano concerto 24','Mozart - Piano Concerto No. 24 in C minor, K. 491 (Mitsuko Uchida)'),(5,'boris brejcha hashtag','Hashtag - Boris Brejcha (Original Mix)'),(6,'nile ithyphallic','Nile - Ithyphallic  HQ'),(7,'unsquare dance','Dave Brubeck - Unsquare Dance'),(8,'get up stand up','Bob Marley - Get Up Stand Up [HQ Sound]'),(9,'the dead south','The Dead South - In Hell I&#39;ll Be In Good Company [Official Music Video]'),(10,'jose feliciano gypsy','Jose Feliciano-Gypsy');
/*!40000 ALTER TABLE `playback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task` (
  `Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Alarm` tinyint(1) DEFAULT '0',
  `Destination` varchar(255) DEFAULT NULL,
  `Due` datetime DEFAULT NULL,
  `StartingLocation` varchar(255) DEFAULT NULL,
  `Text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` (`Id`, `Alarm`, `Destination`, `Due`, `StartingLocation`, `Text`) VALUES (1,0,'faks','2019-06-06 12:35:00','gajba','Odradi projekat do kraja!'),(2,1,'menza','2019-06-06 13:00:00','gajba','Idi u menzu.'),(3,0,'zivot','2019-10-01 00:00:00','faks','Zavrsi faks!'),(4,0,'','2019-06-08 09:00:00','','uci za ispitni rok'),(6,0,'','2019-06-06 12:41:32','','TESTIRAJ PROJEKAT'),(8,0,'','2019-06-06 22:37:08','','test');
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-06-06 22:56:39
