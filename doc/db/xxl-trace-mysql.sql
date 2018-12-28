CREATE database if NOT EXISTS `xxl-trace` default character set utf8 collate utf8_general_ci;
use `xxl-trace`;


CREATE TABLE `xxl_trace_common_registry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(255) NOT NULL COMMENT '注册Key',
  `data` text NOT NULL COMMENT '注册Value有效数据',
  PRIMARY KEY (`id`),
  UNIQUE KEY `I_k` (`key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `xxl_trace_common_registry_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(255) NOT NULL COMMENT '注册Key',
  `value` varchar(255) NOT NULL COMMENT '注册Value',
  `updateTime` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `I_k_v` (`key`,`value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `xxl_trace_common_registry_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` text NOT NULL COMMENT '消息内容',
  `addTime` datetime NOT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
