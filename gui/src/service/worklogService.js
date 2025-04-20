import {syncStatus} from "../consts/syncStatus.js";

const worklogService = {
  processData(worklogs, timeLogs, selectedTickets) {
    const filteredWorklogs = this.filterByTickets(worklogs, selectedTickets);
    return this.sortWorklogsByTimeLogsOrder(filteredWorklogs, timeLogs)
  },

  filterByTickets(worklogs, tickets) {
    if (tickets.length === 0) {
      return worklogs;
    } else {
      return worklogs.filter(worklog => tickets.some(ticket => worklog.ticket === (ticket)));
    }
  },

  sortWorklogsByTimeLogsOrder(worklogs, timeLogs) {
    const newWorklogs = [];
    for (let timeLog of timeLogs) {
      for (let worklog of worklogs) {
        if(worklog.jiraSyncInfo.color === timeLog.jiraSyncInfo.color && worklog.jiraSyncInfo.status === syncStatus.SYNCED) {
          newWorklogs.push(worklog);
          worklogs = worklogs.filter(worklog1 => worklog1 !== worklog);
        }
      }
    }
    newWorklogs.push(...worklogs);
    return newWorklogs;
  }
}

export default worklogService;