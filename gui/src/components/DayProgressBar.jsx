import dayjs from 'dayjs';
import '../styles/ProgressBar.css';
import dateTimeService from "../service/dateTimeService.js";
import minMax from "dayjs/plugin/minMax";

dayjs.extend(minMax);

const DayProgressBar = ({timeLogs, date, setHoveredTimeLogIds}) => {
  const start = dayjs(date, "YYYY-MM-DD")
    .set("hour", dateTimeService.getStartOfDay().get("hour"))
    .set("minute", 0);
  const end = start.add(1, "day");
  const minutesInDay = 1440;

  function createIntervals() {
    const intervals = [];

    timeLogs.forEach(timeLog => {
        if (timeLog.startTime && timeLog.endTime) {
          const segments = splitIntoSegments(timeLog);
          segments.forEach(segment => {
            const interval = {
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
        inactiveSegments.push({startTime: lastEnd, endTime: interval.startTime, color: "gray"});
      }
      lastEnd = interval.endTime;
    });

    if (lastEnd.isBefore(end)) {
      inactiveSegments.push({startTime: lastEnd, endTime: end, color: "gray"});
    }

    return inactiveSegments.concat(intervals).map(interval => {
      return {
        ...interval,
        width: interval.endTime.diff(interval.startTime, "minute") / minutesInDay * 100,
        left: interval.startTime.diff(start, "minute") / minutesInDay * 100
      }
    });
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
    let segments = [{startTime, endTime, color: "blue", id: [timeLog.id]}];

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
                  id: [...segment.id],
                  startTime: segment.startTime,
                  endTime: otherStartTime,
                  color: segment.color
                });
              }

              newSegments.push({
                id: [...segment.id, otherTimeLog.id],
                startTime: dayjs.max(segment.startTime, otherStartTime),
                endTime: dayjs.min(segment.endTime, otherEndTime),
                color: "red"
              });

              if (segment.endTime.isAfter(otherEndTime)) {
                newSegments.push({
                  id: [...segment.id],
                  startTime: dayjs.max(segment.startTime, otherEndTime),
                  endTime: segment.endTime,
                  color: segment.color
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

  return (
    <div className="progress-bar">
      {createIntervals().map((interval, index) =>
        <div
          key={index}
          className="progress-segment"
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