import {syncStatus} from "../consts/syncStatus.js";

const worklogService = {
  filterByTickets(worklogs, tickets) {
    if (tickets.length === 0) {
      return worklogs;
    } else {
      let filtered = worklogs.filter(timeLog => tickets.some(ticket => timeLog.ticket?.startsWith(ticket)));
      if (tickets.includes("Without ticket")) {
        filtered = filtered.concat(this.getAllWithoutTicket(worklogs))
      }
      return filtered
    }
  },

  getAllWithoutTicket(worklogs) {
    return worklogs.filter(worklog => worklog.ticket === null);
  },

  sortWorklogsByTimeLogs(worklogs, timeLogs) {
    const newWorklogs = [];
    for (let timeLog of timeLogs) {
      for (let worklog of worklogs) {
        if(worklog.color === timeLog.color && worklog.syncStatus === syncStatus.SYNCED) {
          newWorklogs.push(worklog);
          worklogs = worklogs.filter(worklog1 => worklog1 !== worklog);
        }
      }
    }
    const nonMatchingWorklogs = worklogs.filter(worklog => worklog.syncStatus !== syncStatus.SYNCED);
    newWorklogs.push(...nonMatchingWorklogs);
    return newWorklogs;
  }
}

export default worklogService;