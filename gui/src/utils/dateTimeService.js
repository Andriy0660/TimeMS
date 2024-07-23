const dateTimeService = {
  getFormattedTime: (time) => time?.format("HH:mm"),
  getFormattedDateTime: (time) => time?.format("YYYY-MM-DDTHH:mm"),
}
export default dateTimeService;
