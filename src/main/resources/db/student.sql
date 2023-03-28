drop table if EXISTS t_student;
CREATE TABLE `t_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `st_name` varchar(20) DEFAULT NULL COMMENT '名字',
  `team` varchar(20) DEFAULT NULL COMMENT '小组',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_ct_by` (create_by) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='学生表';
