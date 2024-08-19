import TimeLogGroupedByDate from "../components/TimeLogGroupedByDate.jsx";
import TimeLogGroupedByDescription from "../components/TimeLogGroupedByDescription.jsx";
import dateTimeService from "./dateTimeService.js";
import {CircularProgress} from "@mui/material";
import NoLogs from "../components/NoLogs.jsx";

const timeLogRenderingService = {
  render({timeLogs, ...props}) {

    switch (JSON.stringify(timeLogs.groupOrder)) {
      case JSON.stringify(["date"]) :
        return timeLogs.data.length ? timeLogs.data
          .sort((a, b) => dateTimeService.compareDates(a.key, b.key))
          .map(({key: date, items: logsForDate}) => (
            <div key={date}>
              <TimeLogGroupedByDate {...props} date={date} logsForDate={logsForDate} />
            </div>
          )) : <NoLogs />
      case JSON.stringify(["date", "description"]) :
        return timeLogs.data.length ? timeLogs.data
          .sort((a, b) => dateTimeService.compareDates(a.key, b.key))
          .map(({key: date, items: logsForDate}) => (
            <div key={date}>
              <TimeLogGroupedByDate {...props} date={date} logsForDate={logsForDate} renderInner={this.renderDescriptionGroup(props)} />
            </div>
          )) : <NoLogs />
      case undefined :
        return (
          <div className="absolute inset-1/2">
            <CircularProgress />
          </div>
        );
      default :
        console.error("No matching render function found for timeLogs:", timeLogs);
        return null;
    }
  },
  renderDescriptionGroup(props) {
    return (description, logsForDescription) => (
      <div key={description}>
        <TimeLogGroupedByDescription {...props} description={description} logsForDescription={logsForDescription} />
      </div>
    );
  }
}

export default timeLogRenderingService;