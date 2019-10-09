CREATE DATABASE `roombooking` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;

CREATE TABLE `roombooking`.`person` (
  `id_person` varchar(10) NOT NULL,
  `name` varchar(45) NOT NULL,
  `lastname` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `role` varchar(45) NOT NULL,
  PRIMARY KEY (`id_person`),
  UNIQUE KEY `id_person_UNIQUE` (`id_person`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table Person. Room Booking Data Base';

CREATE TABLE `roombooking`.`building` (
  `id_building` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `address` varchar(45) NOT NULL,
  PRIMARY KEY (`id_building`),
  UNIQUE KEY `id_building_UNIQUE` (`id_building`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table Building. Room Booking Data Base';


CREATE TABLE `roombooking`.`room` (
  `id_room` int(11) NOT NULL,
  `floor` int(11) DEFAULT NULL,
  `num_room` varchar(45) DEFAULT NULL,
  `max_person` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_room`),
  UNIQUE KEY `id_room_UNIQUE` (`id_room`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table Room. Room Booking Data Base';


CREATE TABLE `roombooking`.`booking` (
  `id_person` varchar(10) NOT NULL,
  `id_room` int(11) NOT NULL,
  `init_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id_person`,`id_room`),
  KEY `fk_idroom_idx` (`id_room`),
  CONSTRAINT `fk_idperson` FOREIGN KEY (`id_person`) REFERENCES `person` (`id_person`),
  CONSTRAINT `fk_idroom` FOREIGN KEY (`id_room`) REFERENCES `room` (`id_room`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table Booking. Room Booking Data Base';


CREATE TABLE `roombooking`.`belong` (
  `id_room` int(11) NOT NULL,
  `id_building` int(11) NOT NULL,
  PRIMARY KEY (`id_room`,`id_building`),
  KEY `fk_idbuilding_idx` (`id_building`),
  CONSTRAINT `fk_id_room` FOREIGN KEY (`id_room`) REFERENCES `room` (`id_room`),
  CONSTRAINT `fk_idbuilding` FOREIGN KEY (`id_building`) REFERENCES `building` (`id_building`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

