import dayjs from "dayjs";
import {endHourOfWorkingDay, startHourOfDay, startHourOfWorkingDay} from "../config/config.js";
import weekday from "dayjs/plugin/weekday"
import {viewMode} from "../consts/viewMode.js";
import timeLogService from "./timeLogService.js";

dayjs.extend(weekday);
dayjs().weekday(1); // Monday

const dateTimeService = {
  getFormattedTime(time) {
    return time && time.isValid() ? time.format("HH:mm") : null;
  },

  getFormattedDate(time) {
    return time && time.isValid() ? time.format("YYYY-MM-DD") : null;
  },

  getFormattedDateTime(time) {
    return time && time.isValid() ? time.format("YYYY-MM-DDTHH:mm") : null;
  },

  isSameDateTime(date1, date2) {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(date2, "second");
  },

  getStartOfDay(date) {
    const baseDate = date ? dayjs(date, "YYYY-MM-DD") : dayjs().startOf("day");
    return baseDate.set("hour", startHourOfDay).set("minute", 0).set("second", 0);
  },

  getWorkingDayInfo(date) {
    return {
      startOfWorkingDay: dayjs(date, "YYYY-MM-DD").set("hour", startHourOfWorkingDay).set("minute", 0).set("second", 0),
      endOfWorkingDay: dayjs(date, "YYYY-MM-DD").set("hour", endHourOfWorkingDay).set("minute", 0).set("second", 0)
    }
  },

  compareTimes(time1, time2) {
    if (time1 === null) return 1;
    if (time2 === null) return -1;
    const t1 = time1.hour() * 60 + time1.minute();
    const t2 = time2.hour() * 60 + time2.minute();
    return t1 - t2;
  },

  buildDate(date, startTime) {
    date = dayjs(date);
    if (startTime && this.compareTimes(dayjs(startTime, "HH:mm"), dayjs().startOf("day")) >= 0 &&
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
    if (timeLogService.getIsTimeLogInNextDayInfo(startTime, null).startTime) {
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

    if (timeLogService.getIsTimeLogInNextDayInfo(startTimeToSet, endTime).endTime) {
      endTime = endTime.add(1, "day");
    }
    return endTime
  },

  formatMinutesToHM(totalMinutes) {
    if(totalMinutes === null) return null;
    return `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m`;
  },

  getMinutesFromHMFormat(timeString) {
    const hoursMatch = parseInt(timeString.match(/(\d+)h/)[1], 10);
    const minutesMatch = parseInt(timeString.match(/(\d+)m/)[1], 10);
    return hoursMatch * 60 + minutesMatch;
  },

  calculateDateRange(mode, date) {
    let startDate, endDate;
    switch (mode) {
      case viewMode.DAY: {
        startDate = date.startOf("day");
        endDate = date.endOf("day").add(1, "second");
        break;
      }
      case viewMode.WEEK: {
         startDate = date.startOf("week");
         endDate = date.endOf("week").add(1, "second");
         break;
      }
      case viewMode.MONTH: {
        startDate = date.startOf("month");
        endDate = date.endOf("month").add(1, "second");
        break;
      }
      default:
        throw new Error("Invalid time mode");
    }
    return {
      startDate: this.getFormattedDate(startDate),
      endDate: this.getFormattedDate(endDate),
    }
  }
}
export default dateTimeService;
