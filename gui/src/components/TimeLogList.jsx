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
        {renderedTimeLogs}
      </div>
    </div>
  );
}
