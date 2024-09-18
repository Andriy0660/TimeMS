import timeLogRenderingService from "../service/timeLogRenderingService.jsx";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onDivide,
  onUpdate,
  onDelete,
  onWorklogCreate,
  setGroupDescription,
  changeDate,
  onSynchronize,
  hoveredTimeLogIds
}) {
  const renderedTimeLogs = timeLogRenderingService.render({
    timeLogs,
    mode,
    onCreate,
    onDivide,
    onUpdate,
    onDelete,
    onWorklogCreate,
    setGroupDescription,
    changeDate,
    onSynchronize,
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
