const dateTimeService = {
  getFormattedTime: (time) => time?.format("HH:mm"),
  getFormattedDate: (time) => time?.format("YYYY-MM-DD"),
  getFormattedDateTime: (time) => time?.format("YYYY-MM-DDTHH:mm"),
  compareTimes: (time1, time2) => {
    const t1 = time1.hour() * 60 + time1.minute();
    const t2 = time2.hour() * 60 + time2.minute();
    return t1 - t2;
  },
}
export default dateTimeService;
