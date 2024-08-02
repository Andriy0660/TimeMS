import dayjs from "dayjs";

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
  compareTimes: (time1, time2) => {
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
  buildStartTime: (date, startTimeToSet) => {
    startTimeToSet = dayjs(startTimeToSet, "HH:mm");
    return startTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", startTimeToSet.get("hour"))
        .set("minute", startTimeToSet.get("minute"))
      : null;
  },
  buildEndTime: (date, startTimeToSet, endTimeToSet) => {
    startTimeToSet = dayjs(startTimeToSet, "HH:mm");
    endTimeToSet = dayjs(endTimeToSet, "HH:mm");
    let endTime = endTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", endTimeToSet.get("hour"))
        .set("minute", endTimeToSet.get("minute"))
      : null;
    if (endTimeToSet && startTimeToSet && endTimeToSet.isBefore(startTimeToSet)) {
      endTime = endTime.add(1, "day");
    }
    return endTime
  }
}
export default dateTimeService;
