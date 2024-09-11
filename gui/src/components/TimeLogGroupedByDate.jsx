import dateTimeService from "../service/dateTimeService.js";
import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";
import dayjs from "dayjs";

export default function TimeLogGroupedByDate({
  date,
  logsForDate,
  renderedInner,
  mode,
  hoveredTimeLogIds,
  setGroupDescription,
  ...apiCalls
}) {

  return (
    <div className="mb-2 shadow-md bg-gray-50">
      {mode !== "Day" &&
        <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{dateTimeService.getFormattedDate(dayjs(date))}</div>}
      {renderedInner ? renderedInner : logsForDate.map((timeLog) =>
        <div key={timeLog.id}>
          <TimeLog
            timeLog={timeLog}
            {...apiCalls}
            setGroupDescription={setGroupDescription}
            hovered={hoveredTimeLogIds.includes(timeLog.id)}
          />
          <Divider />
        </div>
      )}
    </div>
  )
}