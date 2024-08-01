import TimeLogGroupedByDate from "../components/TimeLogGroupedByDate.jsx";
import TimeLogGroupedByDateAndDescription from "../components/TimeLogGroupedByDateAndDescription.jsx";

const timeLogRenderingService = {
  render(props) {
    switch (JSON.stringify(props.timeLogs.groupOrder)) {
      case JSON.stringify(["date"]) :
        return <TimeLogGroupedByDate {...props} />
      case JSON.stringify(["date", "description"]) :
        return <TimeLogGroupedByDateAndDescription {...props} />
      default :
        console.error("No matching render function found for timeLogs:", props.timeLogs);
        return null;
    }
  },
}

export default timeLogRenderingService;