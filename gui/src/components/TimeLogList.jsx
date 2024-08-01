import timeLogRenderingService from "../service/timeLogRenderingService.jsx";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete
}) {
  const renderedTimeLogs = timeLogRenderingService.render({
    timeLogs,
    mode,
    onCreate,
    onUpdate,
    onDelete
  })
  return (
    <div className="m-4 flex flex-col items-center">
      <div className="w-3/5 overflow-x-auto">
        {Object.keys(timeLogs.data).length > 0 ? renderedTimeLogs :
          <div className="p-1 text-center italic ">
            <div className="shadow-md bg-gray-50">No logs...</div>
          </div>}
      </div>
    </div>
  );
}
