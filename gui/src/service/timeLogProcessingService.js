import dateTimeService from "./dateTimeService.js";
import dayjs from "dayjs";

const timeLogProcessingService = {
  group(data, groupOrder) {
    let res = data;
    groupOrder.forEach(groupBy => {
      res = this.groupBy(res, groupBy);
    })
    return {
      data: this.formatGroupedData(res),
      groupOrder
    };
  },
  groupBy(data, key) {
    return this.groupNested(data, key);
  },
  groupNested(data, key) {
    if (!Array.isArray(data)) {
      const groupedData = {};
      Object.keys(data).forEach(groupKey => {
        groupedData[groupKey] = this.groupNested(data[groupKey], key);
      });
      return groupedData;
    } else {
      return this.groupList(data, key);
    }
  },
  groupList(data, key) {
    const groupedData = data.reduce((result, item) => {
      const groupKey = item[key];
      if (!result[groupKey]) {
        result[groupKey] = [];
      }
      result[groupKey].push(item);
      return result;
    }, {});
    Object.keys(groupedData).forEach(groupKey => {
      groupedData[groupKey] = this.sortTimeLogs(groupedData[groupKey]);
    });
    return groupedData;
  },
  sortTimeLogs(data) {
    const getDiffInMinutes = (time) => {
      if (!time) {
        return Number.MAX_SAFE_INTEGER;
      }
      const startOfDay = dateTimeService.getStartOfDay();
      return dateTimeService.compareTimes(dayjs(time), startOfDay) < 0 ? dayjs(time).add(1, "day").diff(startOfDay, "minutes")
        : dayjs(time).diff(startOfDay, "minutes");
    }
    return data.sort((a, b) => getDiffInMinutes(a.startTime) - getDiffInMinutes(b.startTime));
  },

  formatGroupedData(groupedData) {
    const formatData = (data) => {
      return Object.keys(data).map(key => {
        const item = {
          key,
          items: Array.isArray(data[key]) ? data[key] : formatData(data[key])
        };
        return item;
      });
    };

    return formatData(groupedData);
  },

  processData(data, selectedTickets) {
    const filteredData = this.filterByTickets(data, selectedTickets);
    const builtTimeLog = this.buildTimeLog(filteredData);
    return this.markAsConflicted(builtTimeLog);
  },

  filterByTickets(timeLogs, tickets) {
    if (tickets.length === 0) {
      return timeLogs;
    } else {
      let filtered = timeLogs.filter(timeLog => tickets.some(ticket => timeLog.ticket?.startsWith(ticket)));
      if (tickets.includes("Without ticket")) {
        filtered = filtered.concat(this.getAllWithoutTicket(timeLogs))
      }
      return filtered
    }
  },
  getAllWithoutTicket(timeLogs) {
    return timeLogs.filter(timeLog => timeLog.ticket === null);
  },

  buildTimeLog(data) {
    let dataNotNull = data ? Array.from(data) : [];

    const getStatus = ({totalTime, startTime}) => {
      if (totalTime) {
        return "Done";
      } else if (startTime) {
        return "InProgress";
      } else return "Pending";
    }

    return dataNotNull.map(timeLog => {
      const date = dateTimeService.buildDate(timeLog.date, timeLog.startTime);
      const startTime = dateTimeService.buildStartTime(date, timeLog.startTime);
      const endTime = dateTimeService.buildEndTime(date, startTime, timeLog.endTime);
      return {
        ...timeLog,
        date,
        startTime,
        endTime,
        status: getStatus(timeLog),
        ticketAndDescription: timeLog.ticket + timeLog.description
      };
    });
  },

  markAsConflicted(data) {
    let markedData = data ? Array.from(data) : [];
    for(let i = 0; i < data.length; i++) {
      const startTime1 = markedData[i].startTime;
      const endTime1 = markedData[i].endTime;
      if(!startTime1 || !endTime1) {
        continue;
      }
      for (let j = 0; j < markedData.length; j++) {
        const startTime2 = markedData[j].startTime;
        const endTime2 = markedData[j].endTime;
        if (!startTime2 || !endTime2) {
          continue;
        }
        if(i !== j && startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2)) {
          markedData[i].isConflicted = true;
          markedData[j].isConflicted = true;

          markedData[i].conflictedIds = markedData[i].conflictedIds ?? [];
          markedData[i].conflictedIds.push(markedData[j].id);
        }
      }
    }
    return markedData;
  },

  extractTickets(timeLogs) {
    const prefixes = timeLogs?.filter(timeLog => timeLog.ticket).map(timeLog => timeLog.ticket.split('-')[0]);
    return [...new Set(prefixes)];
  },
};

export default timeLogProcessingService;