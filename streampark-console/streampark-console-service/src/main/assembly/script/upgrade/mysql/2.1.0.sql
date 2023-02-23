/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

-- ISSUE-2192 DDL & DML Start

alter table `t_flink_savepoint` modify column `path`  varchar(1024) collate utf8mb4_general_ci default null;

insert into `t_menu` values (100070, 100015, 'savepoint trigger', null, null, 'savepoint:trigger', null, '1', 1, null, now(), now());

-- ISSUE-2192 DDL & DML End


-- ISSUE-2366 DDL & DML Start
alter table `t_flink_app` change column `launch` `release` tinyint default 1;
update `t_menu` set `menu_name`='release',`perms` = 'app:release' where `menu_id` = 100025;
-- ISSUE-2366 DDL & DML End


-- Issue-2324 Start --

insert into `t_menu` values (100071, 100033, 'add yarn queue', null, null, 'yarnQueue:create', '', '1', 0, null, now(), now());
insert into `t_menu` values (100072, 100033, 'edit yarn queue', null, null, 'yarnQueue:update', '', '1', 0, null, now(), now());

-- ----------------------------
-- table structure for t_yarn_queue
-- ----------------------------
drop table if exists `t_yarn_queue`;
create table `t_yarn_queue` (
  `id` bigint not null primary key auto_increment comment 'queue id',
  `team_id` bigint not null comment 'team id',
  `queue_label` varchar(255) collate utf8mb4_general_ci not null comment 'queue and label expression',
  `description` varchar(512) collate utf8mb4_general_ci default null comment 'description of the queue label',
  `create_time` datetime not null default current_timestamp comment 'create time',
  `modify_time` datetime not null default current_timestamp on update current_timestamp comment 'modify time',
   unique key `unq_team_id_queue_label` (`team_id`, `queue_label`) using btree
) engine = innodb default charset = utf8mb4 collate = utf8mb4_general_ci;

-- Issue-2324 End --
