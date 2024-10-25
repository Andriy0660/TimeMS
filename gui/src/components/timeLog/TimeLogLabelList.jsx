import TimeLogLabel from "./TimeLogLabel.jsx";
import dayjs from "dayjs";

export default function TimeLogLabelList({className, labels, onUpdate, timeLog, wrap = false}) {

  return (
    <div className={`${className} overflow-x-hidden flex ${wrap ? "flex-wrap" : ""}`}>
      {labels.map((label, index) => {
        const handleRemove = () => {
          onUpdate({
            id: timeLog.id,
            ticket: timeLog.ticket,
            startTime: dayjs(timeLog.startTime),
            endTime: dayjs(timeLog.endTime),
            labels: labels.filter(l => l !== label)
          });
        }

        return (
          <TimeLogLabel key={index} label={label} onRemove={handleRemove} className="my-1 mx-0.5 first:ml-0 last:mr-0" />
        );
      })}
    </div>
  )
}