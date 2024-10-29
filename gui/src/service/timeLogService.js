import dateTimeService from "./dateTimeService.js";
import {timeLogStatus} from "../consts/timeLogStatus.js";
import dayjs from "dayjs";

const timeLogService = {
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
    return data.reduce((result, item) => {
      const groupKey = item[key];
      if (!result[groupKey]) {
        result[groupKey] = [];
      }
      result[groupKey].push(item);
      return result;
    }, {});
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
    let processedData = data ? Array.from(data) : [];
    if (selectedTickets) {
      processedData = this.filterByTickets(data, selectedTickets);
    }
    const builtTimeLog = this.buildTimeLog(processedData);
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

    const getStatus = ({startTime, endTime}) => {
      if (startTime && endTime) {
        return timeLogStatus.DONE;
      } else if (startTime && !endTime) {
        return timeLogStatus.IN_PROGRESS;
      } else return timeLogStatus.PENDING;
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
    if (timeLogs?.some(timeLog => timeLog.ticket === null)) {
      prefixes.push("Without ticket");
    }
    return [...new Set(prefixes)];
  },

  getIsTimeLogInNextDayInfo(startTime, endTime) {
    const isNextDay = (dateTime) => {
      return dateTime && dateTime.isValid() ?
        dateTimeService.compareTimes(dateTime, dateTimeService.getStartOfDay()) < 0 && dateTimeService.compareTimes(dateTime, dayjs().startOf("day")) >= 0
        : false;
    }
    return {
      startTime: isNextDay(startTime),
      endTime: (endTime && isNextDay(startTime)) || isNextDay(endTime) || (startTime && endTime && dateTimeService.compareTimes(startTime, endTime) > 0)
    }
  },

  getTotalMinutesForTimeLogsArray(timelogsArr, cf = 1) {
    let totalMinutes = 0;
    timelogsArr.forEach(timeLog => totalMinutes += timeLog.endTime?.diff(timeLog.startTime, "minute") || 0);
    return Math.round(totalMinutes / cf);
  },

  getTotalSecondsForTimeLogsArray(timelogsArr) {
    let totalSeconds = 0;
    timelogsArr.forEach(timeLog => totalSeconds += timeLog.endTime?.diff(timeLog.startTime, "second") || 0);
    return Math.round(totalSeconds);
  },

};

export default timeLogService;