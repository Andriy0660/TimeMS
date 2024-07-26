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
  function buildTime(date, startTimeFromDb, endTimeFromDb) {
    startTimeFromDb = dayjs(startTimeFromDb, "HH:mm");
    endTimeFromDb = dayjs(endTimeFromDb, "HH:mm");
    const startTime = startTimeFromDb.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", startTimeFromDb.get("hour"))
        .set("minute", startTimeFromDb.get("minute"))
      : null;
    let endTime = endTimeFromDb.isValid() ? dayjs(date, "YYYY-MM-DD")
        .set("hour", endTimeFromDb.get("hour"))
        .set("minute", endTimeFromDb.get("minute"))
      : null;
    if (endTimeFromDb && startTimeFromDb && endTimeFromDb.isBefore(startTimeFromDb)) {
      endTime = endTime.add(1, "day");
    }
    return {startTime, endTime}
  }

  const renderedTimeLogs = Object.keys(timeLogs).map(date => {
    const logsForDate = timeLogs[date];
    return (
      <div key={date} className="mb-2 w-3/5 shadow-md bg-gray-50">
        {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
        {logsForDate.map((timeLog) => {
          const {startTime, endTime} = buildTime(date, timeLog.startTime, timeLog.endTime, "HH:mm");
          timeLog.startTime = startTime;
          timeLog.endTime = endTime;
          return (
            <div key={timeLog.id}>
              <Divider />
              <TimeLog
                timeLog={timeLog}
                onCreate={onCreate}
                onUpdate={onUpdate}
                onDelete={onDelete}
              />
            </div>
          )
        })}
      </div>
    );
  });

  return (
    <div className="m-4 flex flex-col items-center">
      {Object.keys(timeLogs).length > 0 ? renderedTimeLogs :
        <div className="w-3/5 shadow-md bg-gray-50 p-1 text-center italic">
          No logs...
        </div>}
    </div>
  );
}
