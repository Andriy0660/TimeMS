import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";
import dateTimeService from "../utils/dateTimeService.js";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete
}) {

  const renderedTimeLogs = Object.keys(timeLogs).map(date => {
    const logsForDate = timeLogs[date];
    return (
      <div key={date} className="mb-2 w-3/5 shadow-md bg-gray-50">
        {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
        {logsForDate.map((timeLog) => {
          const startTime = dateTimeService.buildStartTime(date, timeLog.startTime);
          const endTime = dateTimeService.buildEndTime(date, timeLog.startTime, timeLog.endTime);

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
