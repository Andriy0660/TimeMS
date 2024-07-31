import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";
import dayjs from "dayjs";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete
}) {
  const buildTime = {
    startTime: (date, startTimeToSet) => {
      startTimeToSet = dayjs(startTimeToSet, "HH:mm");
      return startTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
          .set("hour", startTimeToSet.get("hour"))
          .set("minute", startTimeToSet.get("minute"))
        : null;
    },
    endTime: (date, startTimeToSet, endTimeToSet) => {
      startTimeToSet = dayjs(startTimeToSet, "HH:mm");
      endTimeToSet = dayjs(endTimeToSet, "HH:mm");
      let endTime = endTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
          .set("hour", endTimeToSet.get("hour"))
          .set("minute", endTimeToSet.get("minute"))
        : null;
      if (endTimeToSet && startTimeToSet && endTimeToSet.isBefore(startTimeToSet)) {
        endTime = endTime.add(1, "day");
      }
      return endTime
    }
  }
  const renderedTimeLogs = timeLogs?.map((timeLog) => {
    const startTime = buildTime.startTime(timeLog.date, timeLog.startTime);
    const endTime = buildTime.endTime(timeLog.date, timeLog.startTime, timeLog.endTime);

    timeLog.startTime = startTime;
    timeLog.endTime = endTime;
    return <div key={timeLog.id}>
      <Divider />
      <TimeLog
        timeLog={timeLog}
        onCreate={onCreate}
        onUpdate={onUpdate}
        onDelete={onDelete}
        buildTime={buildTime}
      />
    </div>
  })

  return (
    <div className="m-4 flex flex-col items-center">
      <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
        {timeLogs.length !== 0 ? renderedTimeLogs :
          <div className="p-1 text-center italic">
            No logs for this day...
          </div>}
      </div>
    </div>
  );
}
