import dateTimeService from "../service/dateTimeService.js";
import TimeLog from "./TimeLog.jsx";
import GroupDescription from "./GroupDescription.jsx";
import {Chip} from "@mui/material";
import Divider from "@mui/material/Divider";
import dayjs from "dayjs";

export default function TimeLogGroupedByDateAndDescription({timeLogs, mode, onCreate, onUpdate, onDelete, setGroupDescription}) {
  const getTotalMinutes = (timeString) => {
    const hoursMatch = parseInt(timeString.match(/(\d+)h/)[1], 10);
    const minutesMatch = parseInt(timeString.match(/(\d+)m/)[1], 10);
    return hoursMatch * 60 + minutesMatch;
  }
  return Object.keys(timeLogs.data)
    .sort((a, b) => dateTimeService.compareDates(a, b))
    .map(date => {
      const logsForDate = timeLogs.data[date];
      return (
        <div key={date} className="mb-2 shadow-md bg-gray-50">
          {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{dateTimeService.getFormattedDate(dayjs(date))}</div>}
          {Object.keys(logsForDate).map(description => {

            const ids = logsForDate[description].reduce((result, item) => {
              result.push(item.id)
              return result;
            }, [])

            const totalTime = logsForDate[description].reduce((result, item) => {
              if (item.status === "Done") {
                result += getTotalMinutes(item.totalTime);
              } else if (item.status === "InProgress") {
                const progressTime = dateTimeService.getDurationOfProgressTimeLog(item.startTime);
                if (progressTime) {
                  result += getTotalMinutes(progressTime);
                }
              }
              return result;
            }, 0)
            const label = `${Math.floor(totalTime / 60)}h ${totalTime % 60}m`;
            return (
              <div key={description}>
                {logsForDate[description].map(timeLog => {

                  return (
                    <div key={timeLog.id}>
                      <TimeLog
                        timeLog={timeLog}
                        onCreate={onCreate}
                        onUpdate={onUpdate}
                        onDelete={onDelete}
                        groupByDescription={true}
                      />

                    </div>
                  );
                })}
                <div className="flex items-center mb-1">
                  <GroupDescription
                    description={description}
                    ids={ids}
                    setGroupDescription={setGroupDescription}
                  />
                  {logsForDate[description].length > 1 &&
                    <Chip
                      label={label}
                      color="primary"
                      variant="outlined"
                      size="small"
                      className="shadow-md mr-2"
                    />
                  }
                </div>
                <Divider />
              </div>
            )
          })}
        </div>
      );
    });
}