import timeLogProcessingService from "./timeLogProcessingService.js";
import dateTimeService from "./dateTimeService.js";
import dayjs from "dayjs";

const fileService = {
  convertToTxt(data) {
    const groupedData = timeLogProcessingService.group(data, ["date"]).data;

    return groupedData
      .sort((a, b) => dateTimeService.compareDates(a.key, b.key))
      .map(({key, items: timeLogs}) => {
        const date = dayjs(key).format("ddd DD.MM.YYYY");

        const formattedLogs = timeLogs.map(timeLog => {
          const formattedStartTime = timeLog.startTime ? timeLog.startTime.format("HH:mm") : "**:**";
          const formattedEndTime = timeLog.endTime ? timeLog.endTime.format("HH:mm") : "**:**"

          const duration = this.getFormattedDuration(timeLog.startTime, timeLog.endTime);
          const ticketPrefix = timeLog.ticket ? `[${timeLog.ticket}]` : "[???]";
          const description = timeLog.description ? timeLog.description : "";

          const symbol = timeLog.ticket ? "+" : "-";
          return `${symbol}  ${formattedStartTime} - ${formattedEndTime} (${duration}) - ${ticketPrefix} ${description}`;
        }).join("\n");

        return `${date}\n${formattedLogs}`;
      }).join("\n\n\n");
  },

  getFormattedDuration(startTime, endTime) {
    const duration = dateTimeService.getDurationInMinutes(startTime, endTime);
    return duration !== null ? this.formatDuration(duration) : "**:**";
  },

  formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${String(hours).padStart(2, '0')}:${String(remainingMinutes).padStart(2, '0')}`;
  }
}
export default fileService;