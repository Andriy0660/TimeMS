import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";

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
