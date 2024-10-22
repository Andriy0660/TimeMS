import dayjs from 'dayjs';
import '../../styles/ProgressBar.css';
import dateTimeService from "../../service/dateTimeService.js";
import minMax from "dayjs/plugin/minMax";
import classNames from "classnames";

dayjs.extend(minMax);

const DayProgressBar = ({timeLogs, date, setHoveredTimeLogIds, hoveredProgressIntervalId}) => {
  const startOfDay = dateTimeService.getStartOfDay(date);
  const endOfDay = startOfDay.add(1, "day");
  const {startOfWorkingDay, endOfWorkingDay} = dateTimeService.getWorkingDayInfo(date);

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

  function buildIntervals(timeLogs) {
    const intervals = [];
    timeLogs.forEach(timeLog => {
      if (timeLog.startTime && timeLog.endTime) {
        splitTimeLogIntoIntervals(timeLog).forEach(interval => {
          intervals.push({
            ...interval,
            color: getColor(interval),
          });
        });
      }
    });
    return intervals;
  }

  function splitTimeLogIntoIntervals(timeLog) {
    const startTime = timeLog.startTime;
    const endTime = timeLog.endTime;
    let intervals = [{startTime, endTime, id: [timeLog.id]}];

    timeLogs.forEach(otherTimeLog => {
      if (timeLog === otherTimeLog) return;
      if (!isOverlapping(timeLog, otherTimeLog)) return;

      intervals = splitAndMergeIntervals(intervals, otherTimeLog);
    });

    return intervals;
  }

  function isOverlapping(timeLog1, timeLog2) {
    return timeLog1.startTime.isBefore(timeLog2.endTime) && timeLog1.endTime.isAfter(timeLog2.startTime);
  }

  function splitAndMergeIntervals(intervals, otherTimeLog) {
    const newIntervals = [];
    const otherStartTime = otherTimeLog.startTime;
    const otherEndTime = otherTimeLog.endTime;

    intervals.forEach(interval => {

      if (!isOverlapping(interval, otherTimeLog)) {
        newIntervals.push(interval);
        return;
      }

      if (interval.startTime.isBefore(otherStartTime)) {
        newIntervals.push({
          ...interval,
          endTime: otherStartTime,
        });
      }

      newIntervals.push({
        ...interval,
        id: [...interval.id, otherTimeLog.id],
        startTime: dayjs.max(interval.startTime, otherStartTime),
        endTime: dayjs.min(interval.endTime, otherEndTime),
      });

      if (interval.endTime.isAfter(otherEndTime)) {
        newIntervals.push({
          ...interval,
          startTime: otherEndTime,
        });
      }
    });

    return newIntervals;
  }

  function getColor(interval) {
    const overlapCount = interval.id.length;
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

  function appendInactiveIntervals(intervals) {
    const inactiveIntervals = [];
    let lastEnd = start;
    intervals.sort((a, b) => a.startTime.diff(b.startTime));
    intervals.forEach(interval => {
      if (lastEnd.isBefore(interval.startTime)) {
        inactiveIntervals.push({startTime: lastEnd, endTime: interval.startTime, color: "gray"})
      }
      lastEnd = interval.endTime;
    });

    if (lastEnd.isBefore(end)) {
      inactiveIntervals.push({startTime: lastEnd, endTime: end, color: "gray"});
    }
    return intervals.concat(inactiveIntervals);
  }

  function splitIntervalsByWorkingHours(intervals) {
    const newIntervals = [];
    intervals.forEach(interval => {
      newIntervals.push(...splitIntervalByWorkingHours(interval));
    })

    return newIntervals;
  }

  function splitIntervalByWorkingHours(interval) {
    const startTime = interval.startTime;
    const endTime = interval.endTime;

    let intervals = [];
    if (startTime.isBefore(startOfWorkingDay)) {
      intervals.push({
        ...interval,
        startTime: startTime,
        endTime: dayjs.min(endTime, startOfWorkingDay),
        thin: true,
      });
    }

    if (startTime.isBefore(endOfWorkingDay) && endTime.isAfter(startOfWorkingDay)) {
      intervals.push({
        ...interval,
        startTime: dayjs.max(startTime, startOfWorkingDay),
        endTime: dayjs.min(endTime, endOfWorkingDay),
        thin: false,

      });
    }

    if (endTime.isAfter(endOfWorkingDay)) {
      intervals.push({
        ...interval,
        startTime: dayjs.max(startTime, endOfWorkingDay),
        endTime: dayjs.min(endTime, endOfDay),
        thin: true,
      });
    }

    if (endTime.isAfter(endOfDay)) {
      intervals.push({
        ...interval,
        startTime: dayjs.max(startTime, endOfDay),
        endTime: endTime,
        thin: true,
        color: "red",
      });
    }

    return intervals;
  }

  function buildUIPosition(intervals) {
    intervals.sort((a, b) => a.startTime.diff(b.startTime));
    return intervals.map((interval, index, array) => {
      let adjustedWidth = interval.endTime.diff(interval.startTime, "minute") / minutesInDay * 100;
      let adjustedLeft = interval.startTime.diff(start, "minute") / minutesInDay * 100;

      const gap = 0.2;
      const prev = array[index - 1];
      if (!interval.id || JSON.stringify(prev?.id) !== JSON.stringify(interval.id)) {
        adjustedWidth -= gap
        adjustedLeft += gap;
      }

      return {
        ...interval,
        width: adjustedWidth,
        left: adjustedLeft,

      };
    })
  }

  let intervals = buildIntervals(timeLogs);
  intervals = appendInactiveIntervals(intervals)
  intervals = splitIntervalsByWorkingHours(intervals)
  intervals = buildUIPosition(intervals)

  return (
    <div className="progress-bar">
      {intervals.map((interval, index) => {
        const isHovered = interval.id?.includes(hoveredProgressIntervalId);
        const intervalClass = classNames({
          'progress-interval' : !isHovered,
          'hovered-progress-interval': isHovered,
          'thin': interval.thin && !isHovered,
          'hovered-thin': interval.thin && isHovered,
        });
        return <div
          key={index}
          className={intervalClass}
          style={{
            width: `${interval.width}%`,
            left: `${interval.left}%`,
            backgroundColor: interval.color,
          }}
          onMouseEnter={() => setHoveredTimeLogIds(Array.from(interval.id || []))}
          onMouseLeave={() => setHoveredTimeLogIds([])}
        />
      })}
    </div>
  );
};

export default DayProgressBar;