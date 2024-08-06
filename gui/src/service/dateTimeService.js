import dayjs from "dayjs";
import {startHourOfDay} from "../config/timeConfig.js";

const dateTimeService = {
  getFormattedTime: (time) => time?.format("HH:mm"),
  getFormattedDate: (time) => time?.format("YYYY-MM-DD"),
  getFormattedDateTime: (time) => time?.format("YYYY-MM-DDTHH:mm"),
  isSameDate: (date1, date2) => {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(date2, "date");
  },
  isSameDateTime: (date1, date2) => {
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
  compareDates: (date1, date2) => {
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
  getDurationOfProgressTimeLog: (startTime) => {
    const currentTime = dayjs();
    const diffInMinutes = currentTime.diff(dayjs(startTime), "minute");
    if(diffInMinutes >= 0 && diffInMinutes < 1440) {
      return `${currentTime.diff(startTime, "hour")}h ${diffInMinutes % 60}m`;
    } else {
      return null;
    }
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
  getTotalMinutes(timeString) {
    const hoursMatch = parseInt(timeString.match(/(\d+)h/)[1], 10);
    const minutesMatch = parseInt(timeString.match(/(\d+)m/)[1], 10);
    return hoursMatch * 60 + minutesMatch;
  },
  getTotalTimeGroupedByDate(timelogs) {
    let totalTime = 0;
    Object.keys(timelogs).forEach(date => {
      const logsForDate = timelogs[date];
      totalTime += logsForDate.reduce((result, item) => {
        if (item.status === "Done") {
          result += dateTimeService.getTotalMinutes(item.totalTime);
        } else if (item.status === "InProgress") {
          const progressTime = dateTimeService.getDurationOfProgressTimeLog(item.startTime);
          if (progressTime) {
            result += dateTimeService.getTotalMinutes(progressTime);
          }
        }
        return result;
      }, 0)
    })
    return totalTime;
  },
  getTotalTimeGroupedByDateAndDescription(timelogs) {
    let totalTime = 0;
    Object.keys(timelogs).forEach(date => {
      const logsForDate = timelogs[date];
      totalTime += this.getTotalTimeGroupedByDate(logsForDate);
    })
    return totalTime;
  },
  getTotalTimeLabel(totalTime) {
    return `${Math.floor(totalTime / 60)}h ${totalTime % 60}m`;
  }
}
export default dateTimeService;
