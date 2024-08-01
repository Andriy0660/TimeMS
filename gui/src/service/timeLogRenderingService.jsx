import dateTimeService from "./dateTimeService.js";
import Divider from "@mui/material/Divider";
import TimeLog from "../components/TimeLog.jsx";
import {Chip} from "@mui/material";
import GroupDescription from "../components/GroupDescription.jsx";

const timeLogRenderingService = {
  render(props) {
    const renderFunctionName = Object.keys(this).find(f => {
      if (!this[f].groupOrder) return;
      return JSON.stringify(this[f].groupOrder) === JSON.stringify(props.timeLogs.groupOrder)
    });
    const renderFunction = this[renderFunctionName].render;
    if (renderFunction) {
      return renderFunction(props);
    } else {
      console.error("No matching render function found for timeLogs:", props.timeLogs);
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
  },
  byDateAndDescription: {
    groupOrder: ["date", "description"],
    render({timeLogs, mode, onCreate, onUpdate, onDelete, setGroupDescription}) {
      return Object.keys(timeLogs.data)
        .sort((a, b) => dateTimeService.compareDates(a, b))
        .map(date => {
          const logsForDate = timeLogs.data[date];
          return (
            <div key={date} className="mb-2 shadow-md bg-gray-50">
              {mode !== "Day" && <div className="ml-1 font-semibold text-gray-500 text-xs font-mono">{date}</div>}
              {Object.keys(logsForDate).map(description => {

                const ids = logsForDate[description].reduce((result, item) => {
                  result.push(item.id)
                  return result;
                }, [])

                const totalTime = logsForDate[description].reduce((result, item) => {
                  const hoursMatch = parseInt(item.totalTime.match(/(\d+)h/)[1], 10);
                  const minutesMatch = parseInt(item.totalTime.match(/(\d+)m/)[1], 10);
                  const totalMinutes = hoursMatch * 60 + minutesMatch;
                  result += totalMinutes;
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
  }
}
export default timeLogRenderingService;