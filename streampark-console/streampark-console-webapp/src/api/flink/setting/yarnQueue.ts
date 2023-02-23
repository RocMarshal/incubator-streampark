/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { defHttp } from '/@/utils/http/axios';
import { Result } from '/#/axios';
import { AxiosResponse } from 'axios';
import { TeamListRecord } from "/@/api/system/model/teamModel";
import { BasicTableParams } from "/@/api/model/baseModel";

enum YARN_QUEUE_API {
  CREATE = '/flink/yarnQueue/post',
  UPDATE = '/flink/yarnQueue/update',
  LIST = '/flink/yarnQueue/list',
  DELETE = '/flink/yarnQueue/delete',
  GET = '/flink/yarnQueue/get',

  CHECK_QUEUE_EXISTED = '/flink/yarnQueue/checkQueueExisted',
  CHECK_QUEUE_LABEL = '/flink/yarnQueue/checkQueueLabel',
}

/**
 * fetch yarn queues in the specified team.
 * @returns Promise<YarnQueue[]>
 */
export function fetchYarnQueueList(data: BasicTableParams): Promise<TeamListRecord[]> {
  return defHttp.post({
    url: YARN_QUEUE_API.LIST,
    data: data,
  });
}

/**
 * fetch yarn queue remove result.
 * @returns {Promise<AxiosResponse<Result>>}
 */
export function fetchYarnQueueDelete(params: { id: string }): Promise<AxiosResponse<Result>> {
  return defHttp.post({ url: YARN_QUEUE_API.DELETE, data: { params } }, { isReturnNativeResponse: true });
}

/**
 * fetch yarn queue label check result.
 */
export function fetchCheckYarnQueueLabel(data: Recordable) {
  return defHttp.post({
    url: YARN_QUEUE_API.CHECK_QUEUE_LABEL,
    data,
  });
}

/**
 * fetch yarn queue existed check result.
 */
export function fetchCheckYarnQueue(data: Recordable) {
  return defHttp.post({
    url: YARN_QUEUE_API.CHECK_QUEUE_EXISTED,
    data,
  });
}

export function fetchYarnQueueCreate(data: Recordable) {
  return defHttp.post({
    url: YARN_QUEUE_API.CREATE,
    data,
  });
}
export function fetchYarnQueueUpdate(data: Recordable) {
  return defHttp.post({
    url: YARN_QUEUE_API.UPDATE,
    data,
  });
}

export function fetchGetYarnQueue(data: Recordable) {
  return defHttp.post({
    url: YARN_QUEUE_API.GET,
    data,
  });
}
