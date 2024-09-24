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
}

export default worklogService;