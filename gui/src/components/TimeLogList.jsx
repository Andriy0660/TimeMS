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

  const renderedTimeLogs = timeLogs?.map((timeLog) => {
    const startTime = dateTimeService.buildStartTime(timeLog.date, timeLog.startTime);
    const endTime = dateTimeService.buildEndTime(timeLog.date, timeLog.startTime, timeLog.endTime);

    timeLog.startTime = startTime;
    timeLog.endTime = endTime;
    return <div key={timeLog.id}>
      <Divider />
      <TimeLog
        timeLog={timeLog}
        onCreate={onCreate}
        onUpdate={onUpdate}
        onDelete={onDelete}
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
