import TimeLog from "./TimeLog.jsx";
import Description from "./Description.jsx";
import {Chip} from "@mui/material";
import Divider from "@mui/material/Divider";
import dateTimeService from "../service/dateTimeService.js";

export default function TimeLogGroupedByDescription({
  description,
  logsForDescription,
  onCreate,
  onDivide,
  onUpdate,
  onDelete,
  setGroupDescription,
  changeDate,
  hoveredTimeLogIds
}) {
  const totalTimeLabel = dateTimeService.formatDuration(dateTimeService.getTotalTimeForTimeLogs(logsForDescription))

  const ids = logsForDescription.reduce((result, item) => {
    result.push(item.id)
    return result;
  }, [])

  return (
    <>
      {logsForDescription.map(timeLog => (
        <div key={timeLog.id}>
          <TimeLog
            timeLog={timeLog}
            onCreate={onCreate}
            onDivide={onDivide}
            onUpdate={onUpdate}
            onDelete={onDelete}
            groupByDescription={true}
            changeDate={changeDate}
            setGroupDescription={setGroupDescription}
            hovered={hoveredTimeLogIds.includes(timeLog.id)} />
        </div>
      ))}
      <div className="flex items-center mb-1">
        <Description className="mx-4" description={description} ids={ids} setGroupDescription={setGroupDescription} />
        {logsForDescription.length > 1 &&
          <Chip label={totalTimeLabel} className="shadow-md mr-2" color="primary" variant="outlined" size="small" />
        }
      </div>
      <Divider />
    </>
  )
}