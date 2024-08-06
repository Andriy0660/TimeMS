import dateTimeService from "./dateTimeService.js";

const timeLogProcessingService = {
  group(data, groupOrder) {
    let res = data;
    groupOrder.forEach(groupBy => {
      res = this.groupBy(res, groupBy);
    })
    return {
      data: res,
      groupOrder
    };
  },
  groupBy(data, key) {
    return this.groupNested(data, key);
  },
  groupNested(data, key) {
    if (!Array.isArray(data)) {
      Object.keys(data).forEach(groupKey => {
        data[groupKey] = this.groupNested(data[groupKey], key);
      });
      return data;
    } else {
      return this.groupList(data, key);
    }
  },
  groupList(data, key) {
    let groupKey;
    const groupedData = data.reduce((result, item) => {
      groupKey = item[key];
      if (!result[groupKey]) {
        result[groupKey] = []
      }
      result[groupKey].push(item);
      return result;
    }, {});
    Object.keys(groupedData).forEach(groupKey => {
      groupedData[groupKey] = this.sortTimeLogs(groupedData[groupKey]);
    });
    return groupedData
  },
  sortTimeLogs(data) {
    const getDiffInMinutes = (time) => {
      if (!time) {
        return Number.MAX_SAFE_INTEGER;
      }
      const startOfDay = dateTimeService.getStartOfDay();
      return dateTimeService.compareTimes(time, startOfDay) < 0 ? time.add(1, "day").diff(startOfDay, "minutes")
        : time.diff(startOfDay, "minutes");
    }
    return data.sort((a, b) => getDiffInMinutes(a.startTime) - getDiffInMinutes(b.startTime));
  },
  processTimeLogDateTime(data) {
    const getStatus = ({totalTime, startTime}) => {
      if (totalTime) {
        return "Done";
      } else if (startTime) {
        return "InProgress";
      } else return "Pending";
    }

    return data.map(timeLog => {
      timeLog.date = dateTimeService.buildDate(timeLog.date, timeLog.startTime);
      timeLog.startTime = dateTimeService.buildStartTime(timeLog.date, timeLog.startTime);
      timeLog.endTime = dateTimeService.buildEndTime(timeLog.date, timeLog.startTime, timeLog.endTime);
      timeLog.status = getStatus(timeLog);
      return timeLog;
    })
  }
};

export default timeLogProcessingService;