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
  },

  parseTimeLogs(content) {
    const lines = content.split('\n');
    const parsedData = [];
    let currentDate = null;

    lines.forEach((line, index) => {
      if (dayjs(line, "ddd DD.MM.YYYY").isValid()) {
        currentDate = dayjs(line, "ddd DD.MM.YYYY");
      } else {
        const logMatch = line.match(/^[+-]\s+(\d{2}:\d{2}|\*\*:\*\*) - (\d{2}:\d{2}|\*\*:\*\*)(.*)\[(.+)\] (.*)$/);
        if (logMatch && currentDate) {
          const startTime = logMatch[1] !== "**:**" ? logMatch[1] : null;
          const endTime = logMatch[2] !== "**:**" ? logMatch[2] : null;
          const ticket = logMatch[4] !== "???" ? logMatch[4] : null;
          const description = logMatch[5] || null;
          if (startTime && dateTimeService.isNextDay(dayjs(startTime, "HH:mm"))) {
            currentDate = currentDate.add(1, "day");
          }
          parsedData.push({
            date: dateTimeService.getFormattedDate(currentDate),
            startTime: startTime,
            endTime: endTime,
            ticket: ticket,
            description: description
          });
        } else {
          throw new Error("Invalid format at line " + (index + 1));
        }
      }
    });

    return parsedData;
  }
}
export default fileService;