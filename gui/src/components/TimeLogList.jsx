import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";

export default function TimeLogList({
  timeLogs,
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
      <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
        {timeLogs.length !== 0 ? renderedTimeLogs :
          <div className="p-1 text-center italic">
            No logs for this day...
          </div>}
      </div>
    </div>
  );
}
