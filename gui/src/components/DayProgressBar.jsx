import dayjs from 'dayjs';
import '../styles/ProgressBar.css';
import dateTimeService from "../service/dateTimeService.js";
import minMax from "dayjs/plugin/minMax";

dayjs.extend(minMax);

const DayProgressBar = ({timeLogs, date, setHoveredTimeLogIds}) => {
  const startOfWorkingDay = dateTimeService.getStartOfWorkingDay(date);
  const endOfWorkingDay = dateTimeService.getEndOfWorkingDay(date);

  let start = startOfWorkingDay;
  let end = endOfWorkingDay;
  for (let i = 0; i < timeLogs.length; i++) {
    if (timeLogs[i].startTime?.isBefore(start)) {
      start = timeLogs[i].startTime;
    }
    if (timeLogs[i].endTime?.isAfter(end)) {
      end = timeLogs[i].endTime;
    }

  }
  start = start.isAfter(startOfWorkingDay) ? startOfWorkingDay : start;
  end = end.isBefore(endOfWorkingDay) ? endOfWorkingDay : end;
  const minutesInDay = end.diff(start, "minutes");

  function createIntervals() {
    const intervals = [];

    timeLogs.forEach(timeLog => {
        if (timeLog.startTime && timeLog.endTime) {
          const segments = splitIntoSegments(timeLog);
          segments.forEach(segment => {
            const interval = {
              ...segment,
              id: segment.id,
              startTime: segment.startTime.isBefore(start) ? start : segment.startTime,
              endTime: segment.endTime.isAfter(end) ? end : segment.endTime,
              color: getColor(segment.id.length)
            }
            intervals.push(interval);
          });
        }
      }
    );
    const inactiveSegments = [];
    let lastEnd = start;
    intervals.sort((a, b) => a.startTime.diff(b.startTime));
    intervals.forEach(interval => {
      if (lastEnd.isBefore(interval.startTime)) {
          inactiveSegments.push(...splitSegmentByWorkingHours(lastEnd, interval.startTime).map(segment => ({
            ...segment,
            color: "gray"
          })));
      }
        lastEnd = interval.endTime;
    });

    if (lastEnd.isBefore(end)) {
      inactiveSegments.push({startTime: lastEnd, endTime: end, color: "gray"});
    }
    return inactiveSegments.concat(intervals).map((interval, index, array) => {
      let adjustedWidth = interval.endTime.diff(interval.startTime, "minute") / minutesInDay * 100;
      let adjustedLeft = interval.startTime.diff(start, "minute") / minutesInDay * 100;

      const gap = 0.1;

      if (index > 0) {
        adjustedLeft += gap;
        adjustedWidth -= gap;
      }

      if (index < array.length - 1) {
        adjustedWidth -= gap;
      }

      return {
        ...interval,
        width: adjustedWidth,
        left: adjustedLeft,
      };
    })
  }

  function getColor(overlapCount) {
    switch (overlapCount) {
      case 1:
        return "blue";
      case 2:
        return "rgba(255, 0, 0, 0.3)";
      case 3:
        return "rgba(255, 0, 0, 0.5)";
      case 4:
        return "rgba(200, 0, 0, 1)";
      default:
        return "rgba(150, 0, 0, 1)";
    }
  }

  function splitIntoSegments(timeLog) {
    const startTime = timeLog.startTime;
    const endTime = timeLog.endTime;
    let segments = [...splitSegmentByWorkingHours(startTime, endTime, timeLog.id)];

    timeLogs.forEach(otherTimeLog => {
      if (timeLog !== otherTimeLog) {
        const otherStartTime = otherTimeLog.startTime;
        const otherEndTime = otherTimeLog.endTime;

        if (startTime.isBefore(otherEndTime) && endTime.isAfter(otherStartTime)) {
          const newSegments = [];
          segments.forEach(segment => {
            if (segment.startTime.isBefore(otherEndTime) && segment.endTime.isAfter(otherStartTime)) {
              if (segment.startTime.isBefore(otherStartTime) && segment.endTime.isAfter(otherStartTime)) {
                newSegments.push({
                  ...segment,
                  id: [...segment.id],
                  startTime: segment.startTime,
                  endTime: otherStartTime,
                });
              }

              newSegments.push({
                ...segment,
                id: [...segment.id, otherTimeLog.id],
                startTime: dayjs.max(segment.startTime, otherStartTime),
                endTime: dayjs.min(segment.endTime, otherEndTime),
              });

              if (segment.endTime.isAfter(otherEndTime)) {
                newSegments.push({
                  ...segment,
                  id: [...segment.id],
                  startTime: dayjs.max(segment.startTime, otherEndTime),
                  endTime: segment.endTime,
                });
              }
            } else {
              newSegments.push(segment);
            }
          });
          segments = newSegments;
        }
      }
    });
    return segments;
  }

  function splitSegmentByWorkingHours(startTime, endTime, id) {
    const segments = [];
    if (startTime.isBefore(startOfWorkingDay)) {
      segments.push({
        startTime: startTime,
        endTime: dayjs.min(endTime, startOfWorkingDay),
        id: [id],
        thin: true
      });
    }

    if (startTime.isBefore(endOfWorkingDay) && endTime.isAfter(startOfWorkingDay)) {
      segments.push({
        startTime: dayjs.max(startTime, startOfWorkingDay),
        endTime: dayjs.min(endTime, endOfWorkingDay),
        id: [id],
        thin: false
      });
    }

    if (endTime.isAfter(endOfWorkingDay)) {
      segments.push({
        startTime: dayjs.max(startTime, endOfWorkingDay),
        endTime: endTime,
        id: [id],
        thin: true
      });
    }
    return segments;
  }

  return (
    <div className="progress-bar">
      {createIntervals().map((interval, index) =>
        <div
          key={index}
          className={`progress-segment ${interval.thin ? "thin" : ""}`}
          style={{
            width: `${interval.width}%`,
            left: `${interval.left}%`,
            backgroundColor: interval.color,
          }}
          onMouseEnter={() => setHoveredTimeLogIds(Array.from(interval.id || []))}
          onMouseLeave={() => setHoveredTimeLogIds([])}
        />
      )}
    </div>
  );
};

export default DayProgressBar;