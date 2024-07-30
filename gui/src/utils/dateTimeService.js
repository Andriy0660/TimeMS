import dayjs from "dayjs";

const dateTimeService = {
  getFormattedTime: (time) => time?.format("HH:mm"),
  getFormattedDate: (time) => time?.format("YYYY-MM-DD"),
  getFormattedDateTime: (time) => time?.format("YYYY-MM-DDTHH:mm"),
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
  }
}
export default dateTimeService;
