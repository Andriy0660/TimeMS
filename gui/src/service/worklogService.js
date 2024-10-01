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
      const matchingWorklogs = worklogs.filter(worklog => worklog.color === timeLog.color);
      newWorklogs.push(...matchingWorklogs);
      worklogs = worklogs.filter(worklog => worklog.color !== timeLog.color);
    }
    return newWorklogs;
  }
}

export default worklogService;