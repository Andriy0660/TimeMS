import dayjs from "dayjs";
import {startHourOfDay} from "../config/timeConfig.js";

const dateTimeService = {
  getFormattedTime(time) {
    return time?.format("HH:mm")
  },
  getFormattedDate(time) {
    return time?.format("YYYY-MM-DD")
  },
  getFormattedDateTime(time) {
    return time?.format("YYYY-MM-DDTHH:mm")
  },
  isSameDate(date1, date2) {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(date2, "date");
  },
  isSameDateTime(date1, date2) {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(date2, "second");
  },
  isTimeLogInNextDay(startTime, endTime) {
    return {
      startTime: this.isNextDay(startTime),
      endTime: (endTime && this.isNextDay(startTime)) || this.isNextDay(endTime) || (startTime && endTime && this.compareTimes(startTime, endTime) > 0)
    }
  },
  isNextDay(dateTime) {
    return dateTime ? this.compareTimes(dateTime, this.getStartOfDay()) < 0 && this.compareTimes(dateTime, dayjs().startOf("day")) > 0 : false;
  },
  getStartOfDay() {
    return dayjs().startOf("day").add(startHourOfDay, "hour");
  },
  compareDates(date1, date2) {
    date1 = dayjs(date1);
    date2 = dayjs(date2);
    if(date1.isSame(date2)) {
      return 0;
    } else if(date1.isBefore(date2)) {
      return -1;
    } else {
      return 1;
    }
  },
  compareTimes(time1, time2) {
    const t1 = time1.hour() * 60 + time1.minute();
    const t2 = time2.hour() * 60 + time2.minute();
    return t1 - t2;
  },
  buildDate(date, startTime) {
    date = dayjs(date);
    if (startTime && this.compareTimes(dayjs(startTime, "HH:mm"), dayjs().startOf("day")) > 0 &&
      this.compareTimes(dayjs(startTime, "HH:mm"), this.getStartOfDay()) < 0) {
      date = date.subtract(1, "day");
    }
    return date;
  },
  buildStartTime(date, startTimeToSet) {
    startTimeToSet = dayjs(startTimeToSet, "HH:mm");
    let startTime = startTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", startTimeToSet.get("hour"))
        .set("minute", startTimeToSet.get("minute"))
      : null;
    if (this.isTimeLogInNextDay(startTime, null).startTime) {
      startTime = startTime.add(1, "day");
    }
    return startTime;
  },
  buildEndTime(date, startTimeToSet, endTimeToSet) {
    startTimeToSet = dayjs(startTimeToSet, "HH:mm");
    endTimeToSet = dayjs(endTimeToSet, "HH:mm");
    let endTime = endTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", endTimeToSet.get("hour"))
        .set("minute", endTimeToSet.get("minute"))
      : null;

    if (this.isTimeLogInNextDay(startTimeToSet, endTime).endTime) {
      endTime = endTime.add(1, "day");
    }
    return endTime
  },
  getTotalTimeForTimeLogs(timelogs) {
    let totalTime = 0;
    totalTime += timelogs.reduce((result, item) => {
      if (item.status === "Done") {
        result += dateTimeService.parseMinutes(item.totalTime);
      } else if (item.status === "InProgress") {
        result += dateTimeService.getDurationInMinutes(item.startTime, null) || 0;
      }
      return result;
    }, 0)
    return totalTime;
  },
  getTotalTimeGroupedByDate(groupedByDate) {
    let totalTime = 0;
    groupedByDate.forEach(({items: logsForDate}) => {
      totalTime += this.getTotalTimeForTimeLogs(logsForDate)
    })
    return totalTime;
  },
  getTotalTimeGroupedByDateAndDescription(groupedByDateAndDescription) {
    let totalTime = 0;
    groupedByDateAndDescription.forEach(({items: logsForDate}) => {
      totalTime += this.getTotalTimeGroupedByDate(logsForDate);
    })
    return totalTime;
  },
  formatDuration(totalMinutes) {
    if(totalMinutes === null) return null;
    return `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m`;
  },
  parseMinutes(timeString) {
    const hoursMatch = parseInt(timeString.match(/(\d+)h/)[1], 10);
    const minutesMatch = parseInt(timeString.match(/(\d+)m/)[1], 10);
    return hoursMatch * 60 + minutesMatch;
  },
  getDurationInMinutes(startTime, endTime) {
    if (!startTime) return null;

    const calculateDuration = (end) => {
      if (end.isBefore(startTime)) return null;
      const minutes = end.diff(startTime, "minute");
      return minutes < 1440 ? minutes : null;
    }

    if (!endTime) {
      const currentTime = dayjs();
      return calculateDuration(currentTime);
    }
    return calculateDuration(endTime);
  },
}
export default dateTimeService;
