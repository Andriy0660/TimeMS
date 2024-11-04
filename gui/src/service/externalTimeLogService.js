import {syncStatus} from "../consts/syncStatus.js";

const externalTimeLogService = {
  processData(externalTimeLogs, timeLogs) {
    return this.sortExternalTimeLogsByTimeLogsOrder(externalTimeLogs, timeLogs)
  },

  sortExternalTimeLogsByTimeLogsOrder(externalTimeLogs, timeLogs) {
    const newExternalTimeLogs = [];
    for (let timeLog of timeLogs) {
      for (let externalTimeLog of externalTimeLogs) {
        if (externalTimeLog.externalTimeLogSyncInfo.color === timeLog.externalTimeLogSyncInfo.color && externalTimeLog.externalTimeLogSyncInfo.status === syncStatus.SYNCED) {
          newExternalTimeLogs.push(externalTimeLog);
          externalTimeLogs = externalTimeLogs.filter(externalTimeLog1 => externalTimeLog1 !== externalTimeLog);
        }
      }
    }
    const nonMatchingExternalTimeLogs = externalTimeLogs.filter(externalTimeLog => externalTimeLog.externalTimeLogSyncInfo.status !== syncStatus.SYNCED);
    newExternalTimeLogs.push(...nonMatchingExternalTimeLogs);
    return newExternalTimeLogs;
  }
}

export default externalTimeLogService;