import TimeLog from "./TimeLog.jsx";
import Divider from "@mui/material/Divider";
import dayjs from "dayjs";

export default function TimeLogList({
  timeLogs,
  mode,
  onCreate,
  onUpdate,
  onDelete,
  isGroupedByDescription
}) {
  const buildTime = {
    startTime: (date, startTimeToSet) => {
      startTimeToSet = dayjs(startTimeToSet, "HH:mm");
      return startTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
          .set("hour", startTimeToSet.get("hour"))
          .set("minute", startTimeToSet.get("minute"))
        : null;
    },
    endTime: (date, startTimeToSet, endTimeToSet) => {
      startTimeToSet = dayjs(startTimeToSet, "HH:mm");
      endTimeToSet = dayjs(endTimeToSet, "HH:mm");
      let endTime = endTimeToSet.isValid() ? dayjs(date, "YYYY-MM-DD")
          .set("hour", endTimeToSet.get("hour"))
          .set("minute", endTimeToSet.get("minute"))
        : null;
      if (endTimeToSet && startTimeToSet && endTimeToSet.isBefore(startTimeToSet)) {
        endTime = endTime.add(1, "day");
      }
      return endTime
    }
  }

  const renderedTimeLogs = Object.keys(timeLogs).map(date => {
    const logsForDate = timeLogs[date];
    return (
      <div key={date} className="mb-2 w-3/5 shadow-md bg-gray-50">
        {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
        {!isGroupedByDescription ? (
            logsForDate.map((timeLog) => {
              const startTime = buildTime.startTime(date, timeLog.startTime);
              const endTime = buildTime.endTime(date, timeLog.startTime, timeLog.endTime);

              timeLog.startTime = startTime;
              timeLog.endTime = endTime;
              return (
                <div key={timeLog.id}>
                  <Divider />
                  <TimeLog
                    timeLog={timeLog}
                    onCreate={onCreate}
                    onUpdate={onUpdate}
                    onDelete={onDelete}
                    buildTime={buildTime}
                  />
                </div>
              )
            })
          )
          :
          (Object.keys(logsForDate).map(description =>
            <div key={description}>
              <Divider/>
              <div className="text-justify whitespace-pre-wrap m-2 ml-4 font-medium">{description}</div>
              {logsForDate[description].map((timeLog, index) => {
                const startTime = buildTime.startTime(date, timeLog.startTime);
                const endTime = buildTime.endTime(date, timeLog.startTime, timeLog.endTime);

                timeLog.startTime = startTime;
                timeLog.endTime = endTime;
                return (
                  <div key={timeLog.id}>
                    <TimeLog
                      timeLog={timeLog}
                      onCreate={onCreate}
                      onUpdate={onUpdate}
                      onDelete={onDelete}
                      buildTime={buildTime}
                      groupByDescription={true}
                    />
                    {index < logsForDate[description].length - 1 && <Divider variant="middle" />}
                  </div>
                );
              })}
            </div>))
        }
      </div>
    );
  });

  return (
    <div className="m-4 flex flex-col items-center">
      {Object.keys(timeLogs).length > 0 ? renderedTimeLogs :
        <div className="w-3/5 shadow-md bg-gray-50 p-1 text-center italic">
          No logs...
        </div>}
    </div>
  );
}
