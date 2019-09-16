-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versión del servidor:         10.1.38-MariaDB - mariadb.org binary distribution
-- SO del servidor:              Win64
-- HeidiSQL Versión:             10.2.0.5599
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Volcando datos para la tabla diverta_cart.products: ~6 rows (aproximadamente)
DELETE FROM `products`;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` (`sku`, `name`, `price`) VALUES
	('0013803307627', 'Canon laser color printer MF644CDW', 48699),
	('0020356620370', 'TDK Blank CD - 100 unit bulk', 1689),
	('0097855121776', 'Logicool BT Keyboard K780 JP/EN Win/Mac', 5999),
	('0619659078645', 'Sandisk microSD card 32GB, Class 10', 990),
	('0763649097977', 'Seagate SkyHawk 1TB internal HDD ST1000VX005 ', 5079),
	('0794163167471', 'HeimVision 1080p WiFi IP Camera', 2999),
	('0886576036403', 'Silicon Power 1TB internal SSD SATA3 SP001TBSS3A55S25', 11399),
	('0994954474003', 'Blenck Wireless 2.4GHz rechargeable mouse', 1759),
	('2082491115966', 'BeWinner PCI-x1 7-port USB3.0 host', 2099),
	('4712900000000', 'ASUS 23 inch Frameless Monitor VZ249HE', 10499),
	('6935364050993', 'TP-LINK WiFi Router AC1200 (802.11ac)', 3399),
	('7130398653298', 'IP67 smartwatch', 2599),
	('8799522981738', 'Buffalo 100M/1G 8-port Ethernet switch', 2279),
	('9990000000000', 'Super combo: 10.1" Lenovo tablet + Samsung 64GB uSD card', 8999);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
