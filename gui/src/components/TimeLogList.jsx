import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";

export default function TimeLogList({
  timeLogs,
  date,
  mode,
  onCreate,
  onUpdate,
  onDelete
}) {
  const renderedTimeLogs = timeLogs?.map((timeLog) => {
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
      {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
      <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
        {timeLogs.length !== 0 ? renderedTimeLogs :
          <div className="p-1 text-center italic">
            No logs...
          </div>}
      </div>
    </div>
  );
}
