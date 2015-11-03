-- MySQL dump 10.13  Distrib 5.5.46, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: fdc
-- ------------------------------------------------------
-- Server version	5.5.46-0ubuntu0.14.04.2

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
-- Table structure for table `Comment`
--

DROP TABLE IF EXISTS `Comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Comment` (
  `id` varchar(255) NOT NULL,
  `message` text,
  `created_at` datetime NOT NULL,
  `likes` int(11) DEFAULT '0',
  `from_id` varchar(255) NOT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `post_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_post_id_idx` (`post_id`),
  CONSTRAINT `fk_comment_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Like`
--

DROP TABLE IF EXISTS `Like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Like` (
  `from_id` varchar(255) NOT NULL,
  `from_name` varchar(255) NOT NULL,
  `post_id` varchar(255) NOT NULL,
  KEY `fk_post_id_idx` (`post_id`),
  CONSTRAINT `fk_like_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Page`
--

DROP TABLE IF EXISTS `Page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Page` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `likes` int(11) DEFAULT '0',
  `talking_about` int(11) DEFAULT '0',
  `checkins` int(11) DEFAULT '0',
  `website` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `affiliation` varchar(255) DEFAULT NULL,
  `about` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `page_UNIQUE` (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PageCrawl`
--

DROP TABLE IF EXISTS `PageCrawl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PageCrawl` (
  `crawl_date` datetime NOT NULL,
  `page_id` varchar(255) NOT NULL,
  `likes` int(11) DEFAULT '0',
  `talking_about` int(11) DEFAULT '0',
  `checkins` int(11) DEFAULT '0',
  PRIMARY KEY (`crawl_date`,`page_id`),
  KEY `fk_page_crawl_page_id_idx` (`page_id`),
  CONSTRAINT `fk_page_crawl_page_id` FOREIGN KEY (`page_id`) REFERENCES `Page` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Post`
--

DROP TABLE IF EXISTS `Post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Post` (
  `id` varchar(255) NOT NULL,
  `page_id` varchar(255) NOT NULL,
  `message` text,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `likes` int(11) DEFAULT '0',
  `comments` int(11) DEFAULT '0',
  `shares` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_page_id_idx` (`page_id`),
  CONSTRAINT `fk_page_id` FOREIGN KEY (`page_id`) REFERENCES `Page` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PostCrawl`
--

DROP TABLE IF EXISTS `PostCrawl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PostCrawl` (
  `crawl_date` datetime NOT NULL,
  `post_id` varchar(255) NOT NULL,
  `likes` int(11) DEFAULT '0',
  `comments` int(11) DEFAULT '0',
  `shares` int(11) DEFAULT '0',
  PRIMARY KEY (`crawl_date`,`post_id`),
  KEY `fk_post_crawl_post_id_idx` (`post_id`),
  CONSTRAINT `fk_post_crawl_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-11-02 17:20:51
