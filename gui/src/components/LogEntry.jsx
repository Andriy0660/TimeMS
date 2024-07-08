import {Button, TextField} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useState} from "react";

export default function LogEntry({logEntry}) {
  const [ticket, setTicket] = useState(logEntry.ticket || "");
  const [startTime, setStartTime] = useState(dayjs(logEntry.startTime, "hh:mm"));
  const [endTime, setEndTime] = useState(logEntry.endTime ? dayjs(logEntry.endTime, "hh:mm") : null);
  const [description, setDescription] = useState(logEntry.description || "");

  return (
    <div onBlur={() => console.log("hello")} className="flex items-center">
      <div className="px-2 py-2">
        <TextField
          className="w-24"
          label="Ticket"
          size="small"
          value={ticket}
          onChange={(event) => setTicket(event.target.value)}
          autoComplete="off"
        />
      </div>
      <div className="px-2 py-2">
        <TimeField
          className="w-20"
          label="Start"
          size="small"
          value={startTime}
          onChange={(date) =>
            setStartTime(dayjs(`${date.$d.getHours()}:${date.$d.getMinutes()}`, "hh:mm"))}
          format="HH:mm"
        />
      </div>
      <div className="px-2 py-2">
        <TimeField
          className="w-20"
          label="End"
          value={endTime}
          onChange={(date) =>
            setEndTime(date ? dayjs(`${date.$d.getHours()}:${date.$d.getMinutes()}`, "hh:mm") : null)}
          size="small"
          format="HH:mm"
        />
      </div>
      <div className="min-w-40 w-full px-2 py-2">
        <TextField
          className="w-full"
          label="Description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          size="small"
          autoComplete="off"
        />
      </div>
      <div className="px-2 py-2">
        {logEntry.totalTime ?
          <Button variant="outlined">Continue</Button> :
          <Button color="warning" variant="outlined">Stop</Button>
        }
      </div>
      <div className="px-2 py-2 text-sm whitespace-nowrap">
        {logEntry.totalTime ?? "In Progress..."}
      </div>
    </div>

  );
}