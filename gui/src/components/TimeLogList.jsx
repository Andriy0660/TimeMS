import timeLogRenderingService from "../service/timeLogRenderingService.jsx";

export default function TimeLogList({
  ...props
}) {
  const renderedTimeLogs = timeLogRenderingService.render({
    ...props
  })
  return (
    <div className="m-4 flex flex-col items-center">
      <div className="w-full overflow-x-auto">
        {renderedTimeLogs}
      </div>
    </div>
  );
}
