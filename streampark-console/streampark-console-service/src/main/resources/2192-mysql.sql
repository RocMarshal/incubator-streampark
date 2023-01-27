drop table if exists `t_flink_savepoint`;
create table `t_flink_savepoint` (
  `id` bigint not null auto_increment,
  `app_id` bigint not null,
  -- new.
  `job_id`           VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  -- new
  `status` VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COMPLETED',
  `chk_id` bigint default null,
  `type` tinyint default null,
  -- update
  `path` varchar(2048) collate utf8mb4_general_ci default null,
  `latest` tinyint not null default 1,
  `trigger_time` datetime default null,
  -- new.
  `execution_mode` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  -- new
  `cluster_id` bigint DEFAULT NULL,
  -- new
  `end_time`    datetime DEFAULT NULL,
  `create_time` datetime not null default current_timestamp comment 'create time',
  -- new
  `modify_time` datetime not null default current_timestamp on update current_timestamp comment 'modify time',
  UNIQUE KEY `unique_app_id_job_id_chk_id` (`app_id`, `job_id`, `chk_id`),
  primary key (`id`) using btree
) engine=innodb auto_increment=100000 default charset=utf8mb4 collate=utf8mb4_general_ci;


alter table `t_flink_savepoint` add column `job_id` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL after `app_id`;
alter table `t_flink_savepoint` add column `status` VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COMPLETED' after `job_id`;
alter table `t_flink_savepoint` add column `execution_mode` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL after `trigger_time`;
alter table `t_flink_savepoint` add column `cluster_id` bigint DEFAULT NULL after `execution_mode`;
alter table `t_flink_savepoint` add column `end_time`    datetime DEFAULT NULL after `cluster_id`;
alter table `t_flink_savepoint` add column `modify_time` datetime not null default current_timestamp on update current_timestamp comment 'modify time' after `create_time`;

alter table `t_flink_savepoint` modify column `path`  varchar(2048) collate utf8mb4_general_ci default null;

alter table `t_flink_savepoint` add UNIQUE KEY `unique_app_id_job_id_chk_id` (`app_id`, `job_id`, `chk_id`);
