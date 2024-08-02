import dateTimeService from "../service/dateTimeService.js";
import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";
import dayjs from "dayjs";

export default function TimeLogGroupedByDate({timeLogs, mode, onCreate, onUpdate, onDelete}) {
  return Object.keys(timeLogs.data)
    .sort((a, b) => dateTimeService.compareDates(a, b))
    .map(date => {
      const logsForDate = timeLogs.data[date];
      return (
        <div key={date} className="mb-2 shadow-md bg-gray-50">
          {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{dateTimeService.getFormattedDate(dayjs(date))}</div>}
          {logsForDate.map((timeLog) => {

            return (
              <div key={timeLog.id}>
                <TimeLog
                  timeLog={timeLog}
                  onCreate={onCreate}
                  onUpdate={onUpdate}
                  onDelete={onDelete}
                />
                <Divider />
              </div>
            )
          })}
        </div>
      );
    });
}