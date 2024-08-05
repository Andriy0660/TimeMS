import timeLogRenderingService from "../service/timeLogRenderingService.jsx";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete,
  setGroupDescription
}) {
  const renderedTimeLogs = timeLogRenderingService.render({
    timeLogs,
    mode,
    onCreate,
    onUpdate,
    onDelete,
    setGroupDescription
  })
  return (
    <div className="m-4 flex flex-col items-center">
      <div className="w-3/5 overflow-x-auto">
        {renderedTimeLogs}
      </div>
    </div>
  );
}
