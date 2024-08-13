import dateTimeService from "./dateTimeService.js";

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
      return dateTimeService.compareTimes(time, startOfDay) < 0 ? time.add(1, "day").diff(startOfDay, "minutes")
        : time.diff(startOfDay, "minutes");
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

  processTimeLogDateTime(data) {
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
        status: getStatus(timeLog)
      };
    });
  }
};

export default timeLogProcessingService;