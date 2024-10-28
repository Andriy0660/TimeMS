import TimeLog from "./TimeLog.jsx";
import TimeLogDescription from "./TimeLogDescription.jsx";
import {Chip} from "@mui/material";
import dateTimeService from "../../service/dateTimeService.js";
import TimeLogGroup from "./TimeLogGroup.jsx";
import timeLogService from "../../service/timeLogService.js";

export default function TimeLogGroupedByDescription({
  description,
  logsForDescription,
  setGroupDescription,
  hoveredTimeLogIds,
  isJiraEditMode,
  ...rest
}) {
  const totalTimeLabel = dateTimeService.formatMinutesToHM(
    timeLogService.getTotalMinutesForTimeLogsArray(logsForDescription)
  );

  const ids = logsForDescription.reduce((result, item) => {
    result.push(item.id)
    return result;
  }, [])

  return (
    <TimeLogGroup isJiraEditMode={isJiraEditMode} className="mb-2">
      {logsForDescription.map(timeLog => (
        <div key={timeLog.id}>
          <TimeLog
            timeLog={timeLog}
            {...rest}
            isJiraEditMode={isJiraEditMode}
            groupByDescription={true}
            setGroupDescription={setGroupDescription}
            hovered={hoveredTimeLogIds?.includes(timeLog.id)} />
        </div>
      ))}
      <div className="flex items-center">
        <TimeLogDescription description={description} ids={ids} setGroupDescription={setGroupDescription} isJiraEditMode={isJiraEditMode}/>
        {logsForDescription.length > 1 &&
          <Chip label={totalTimeLabel} className="shadow-md ml-2" color="primary" variant="outlined" size="small" />
        }
      </div>
    </TimeLogGroup>
  )
}