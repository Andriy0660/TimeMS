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
      if (line.trim().length === 0) return;

      const date = parsers.reduce((date, parser) => date || parser.getDate(line), null);
      if (date) {
        currentDate = date;
      } else {
        const parser = parsers.find(p => p.isValid(line));
        if (!parser) {
          console.log(line);
          throw new Error("Invalid format at line " + (index + 1));
        }
        parsedData.push(parser.parse(currentDate, line.match(parser.regex)));
      }
    });

    return parsedData;
  },

}

const igorRegex = /[+-] {2}(\d{2}:\d{2}|\*\*:\*\*) - (\d{2}:\d{2}|\*\*:\*\*) \(.*\) - \[(.+)\] (.*)/;
const parsers = [
  {
    isValid: isValidIgorFormat,
    parse: parseIgorFormat,
    getDate: getIgorDate,
    regex: igorRegex
  }
];

function parseIgorFormat(currentDate, match) {
  const startTime = dayjs(match[1] !== "**:**" ? match[1] : null, "HH:mm");
  const endTime = dayjs(match[2] !== "**:**" ? match[2] : null, "HH:mm");
  const ticket = match[3] !== "???" ? match[3] : null;
  const description = match[4] || null;
  let date = currentDate;
  if (dateTimeService.isNextDay(startTime)) {
    date = date.add(1, "day");
  }
  return {
    date: dateTimeService.getFormattedDate(date),
    startTime: dateTimeService.getFormattedTime(startTime),
    endTime: dateTimeService.getFormattedTime(endTime),
    ticket, description
  }
}

function isValidIgorFormat(line) {
  return igorRegex.test(line);
}

function getIgorDate(line) {
  const datePattern = /\w{3} \d{2}\.\d{2}\.\d{4}/;

  const dateMatch = line.match(datePattern);
  if (dateMatch) {
    const date = dayjs(dateMatch[0], "ddd DD.MM.YYYY");
    return date.isValid() ? date : null;
  }

  return null;
}

export default fileService;