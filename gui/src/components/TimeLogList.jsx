import timeLogRenderingService from "../service/timeLogRenderingService.jsx";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete,
  setGroupDescription,
  changeDate,
  hoveredTimeLogIds
}) {
  const renderedTimeLogs = timeLogRenderingService.render({
    timeLogs,
    mode,
    onCreate,
    onUpdate,
    onDelete,
    setGroupDescription,
    changeDate,
    hoveredTimeLogIds
  })
  return (
    <div className="m-4 flex flex-col items-center">
      <div className="w-full overflow-x-auto">
        {renderedTimeLogs}
      </div>
    </div>
  );
}
