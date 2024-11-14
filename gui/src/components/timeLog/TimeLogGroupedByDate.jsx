import dateTimeService from "../../service/dateTimeService.js";
import TimeLog from "./TimeLog.jsx";
import dayjs from "dayjs";
import TimeLogGroup from "./TimeLogGroup.jsx";
import {viewMode} from "../../consts/viewMode.js";

export default function TimeLogGroupedByDate({
  date,
  logsForDate,
  renderedInner,
  mode,
  hoveredTimeLogIds,
  setGroupDescription,
  isInEditMode,
  ...rest
}) {

  return (
    <div>
      {mode !== viewMode.DAY &&
        <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{dateTimeService.getFormattedDate(dayjs(date))}</div>}
      <div>
        {renderedInner ? renderedInner : logsForDate.map((timeLog) =>

          <TimeLogGroup key={timeLog.id} isInEditMode={isInEditMode} className="mb-2">
            <TimeLog
              timeLog={timeLog}
              isInEditMode={isInEditMode}
              {...rest}
              setGroupDescription={setGroupDescription}
              hovered={hoveredTimeLogIds?.includes(timeLog.id)}
            />
          </TimeLogGroup>
        )}
      </div>
    </div>
  )
}