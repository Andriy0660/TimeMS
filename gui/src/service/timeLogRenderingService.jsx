import TimeLogGroupedByDate from "../components/timeLog/TimeLogGroupedByDate.jsx";
import TimeLogGroupedByDescription from "../components/timeLog/TimeLogGroupedByDescription.jsx";
import NoLogs from "../components/general/NoLogs.jsx";
import LoadingPage from "../components/general/LoadingPage.jsx";

const timeLogRenderingService = {
  render({timeLogs, ...props}) {

    switch (JSON.stringify(timeLogs.groupOrder)) {

      case JSON.stringify(["date"]) :
        return timeLogs.data.length ? timeLogs.data
          .map(({key: date, items: logsForDate}) => (
            <div key={date}>
              <TimeLogGroupedByDate {...props} date={date} logsForDate={logsForDate} />
            </div>
          )) : <NoLogs />

      case JSON.stringify(["date", "ticketAndDescription"]) :
        return timeLogs.data.length ? timeLogs.data
          .map(({key: date, items: logsForDate}) => (
            <div key={date}>
              <TimeLogGroupedByDate {...props} date={date} logsForDate={logsForDate} renderedInner={this.renderedDescriptionGroup(props, logsForDate)} />
            </div>
          )) : <NoLogs />

      case undefined :
        return <LoadingPage />

      default :
        console.error("No matching render function found for timeLogs:", timeLogs);
        return null;
    }
  },
  renderedDescriptionGroup(props, logsForDate) {
    return logsForDate.map(({key, items: logsForDescription}) => (
      <div key={key}>
        <TimeLogGroupedByDescription {...props} description={logsForDescription[0].description} logsForDescription={logsForDescription} />
      </div>
    ));
  }
}

export default timeLogRenderingService;