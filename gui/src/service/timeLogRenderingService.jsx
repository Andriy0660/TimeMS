import dateTimeService from "./dateTimeService.js";
import Divider from "@mui/material/Divider";
import TimeLog from "../components/TimeLog.jsx";

const timeLogRenderingService = {
  render({timeLogs, mode, onCreate, onUpdate, onDelete}) {
    const renderFunctionName = Object.keys(this).find(f=> {
      if(!this[f].groupOrder) return;
      return JSON.stringify(this[f].groupOrder) === JSON.stringify(timeLogs.groupOrder)
    });
    const renderFunction = this[renderFunctionName].render;
    if (renderFunction) {
      return renderFunction({timeLogs, mode, onCreate, onUpdate, onDelete});
    } else {
      console.error("No matching render function found for timeLogs:", timeLogs);
      return null;
    }
  },
  byDate: {
    groupOrder: ["date"],
    render({timeLogs, mode, onCreate, onUpdate, onDelete}) {
      return Object.keys(timeLogs.data)
        .sort((a, b) => dateTimeService.compareDates(a, b))
        .map(date => {
          const logsForDate = timeLogs.data[date];
          return (
            <div key={date} className="mb-2 shadow-md bg-gray-50">
              {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
              {logsForDate.map((timeLog) => {

                return (
                  <div key={timeLog.id}>
                    <Divider />
                    <TimeLog
                      timeLog={timeLog}
                      onCreate={onCreate}
                      onUpdate={onUpdate}
                      onDelete={onDelete}
                    />
                  </div>
                )
              })}
            </div>
          );
        });
    }
  },
  byDateAndDescription: {
    groupOrder: ["date", "description"],
    render({timeLogs, mode, onCreate, onUpdate, onDelete}) {
      return Object.keys(timeLogs.data)
        .sort((a, b) => dateTimeService.compareDates(a, b))
        .map(date => {
          const logsForDate = timeLogs.data[date];
          return (
            <div key={date} className="mb-2 shadow-md bg-gray-50">
              {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
              {Object.keys(logsForDate).map(description =>
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
                  <div className="text-justify whitespace-pre-wrap mx-4 mb-1">{description}</div>
                  <Divider />
                </div>)
              }
            </div>
          );
        });
    }
  }
}
export default timeLogRenderingService;