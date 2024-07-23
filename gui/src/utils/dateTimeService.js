const dateTimeService = {
  getFormattedTime: (time) => time?.format("HH:mm"),
  getFormattedDate: (time) => time?.format("YYYY-MM-DD"),
  getFormattedDateTime: (time) => time?.format("YYYY-MM-DDTHH:mm"),
}
export default dateTimeService;
