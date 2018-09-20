CREATE TABLE `merchants`(
  `id` INT(10) unsigned NOT NULL auto_increment,
  `name` VARCHAR(64) COLLATE utf8_bin NOT NULL comment '商户名称',
  `logo_url` VARCHAR(256) COLLATE utf8_bin NOT NULL comment '商户 logo',
  `business_license_url` VARCHAR(256) COLLATE utf8_bin NOT NULL comment '商户营业执照',
  `phone` VARCHAR(64) COLLATE utf8_bin NOT NULL comment '商户联系电话',
  `address` VARCHAR(64) COLLATE utf8_bin NOT NULL comment '商户地址',
  `is_audit` BOOLEAN NOT NULL comment '是否通过审核',
  PRIMARY KEY(`id`)
)ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;