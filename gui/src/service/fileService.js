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
    const igorFormatRegex = /[+-]\s+(\d{2}:\d{2}|\*\*:\*\*) - (\d{2}:\d{2}|\*\*:\*\*).*\[(.+)\] (.*)/;
    const andriiFormatRegex = /(.*?) -\s*(?:(.*)\(.*\) - )?(.*)?/;

    const lines = content.split('\n');
    const parsedData = [];
    let currentDate = null;

    lines.forEach((line, index) => {
      if (line.trim().length === 0) return;

      const date = this.getDate(line);
      if (date) {
        currentDate = date;
      } else {
        if (!currentDate || (!igorFormatRegex.test(line) && !andriiFormatRegex.test(line))) {
          throw new Error("Invalid format at line " + (index + 1));
        }
        if (igorFormatRegex.test(line)) {
          parsedData.push(this.getTimeLogFromIgorFormat(currentDate, line.match(igorFormatRegex)));
        } else {
          parsedData.push(this.getTimeLogFromAndriiFormat(currentDate, line.match(andriiFormatRegex)));
        }
      }
    });

    return parsedData;
  },

  getDate(line) {
    const getIgorDate = (line) => dayjs(line, "ddd DD.MM.YYYY");
    const getAndriiDate = (line) => dayjs(line.replace(/-/g, ''), "DD.MM.YYYY");

    const igorDate = getIgorDate(line);
    if (igorDate.isValid()) {
      return igorDate;
    }

    const andriiDate = getAndriiDate(line);
    if (andriiDate.isValid()) {
      return andriiDate;
    }

    return null;
  },

  getTimeLogFromIgorFormat(currentDate, logMatch) {
    const startTime = dayjs(logMatch[1] !== "**:**" ? logMatch[1] : null, "HH:mm");
    const endTime = dayjs(logMatch[2] !== "**:**" ? logMatch[2] : null, "HH:mm");
    const ticket = logMatch[3] !== "???" ? logMatch[3] : null;
    const description = logMatch[4] || null;
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
  },

  getTimeLogFromAndriiFormat(currentDate, logMatch) {
    const startTime = this.parseAndriiTime(logMatch[1]);
    const endTime = this.parseAndriiTime(logMatch[2]);
    const description = logMatch[3] || null;
    let date = currentDate;
    if (dateTimeService.isNextDay(startTime)) {
      date = date.add(1, "day");
    }
    return {
      date: dateTimeService.getFormattedDate(date),
      startTime: dateTimeService.getFormattedTime(startTime),
      endTime: dateTimeService.getFormattedTime(endTime),
      description
    }
  },
  parseAndriiTime(timeString) {
    if (!timeString) return null;
    if (!timeString.includes(".")) {
      timeString += ".00";
    }
    return dayjs(timeString, "H:m");
  }
}
export default fileService;