import LogEntry from "./LogEntry.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {Button, TextField} from "@mui/material";
import {LocalizationProvider, TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import Divider from "@mui/material/Divider";

const logEntries = [
  {
    id: 1,
    ticket: "LRN-34",
    startTime: "12:34",
    endTime: "17:12",
    description: "made gui mockup",
    totalTime: "4h 13m"
  },
  {
    id: 2,
    ticket: "",
    startTime: "18:00",
    endTime: "20:13",
    description: "made gui mockup and some other big amount of work with some problems",
    totalTime: "2h 13m"
  },
  {
    id: 3,
    ticket: "LRN-34",
    startTime: "2024-07-08T09:28:33.388463",
    description: ""
  },
]
export default function LogEntryList({}) {
  const renderedLogEntries = logEntries.map((logEntry) => {
    return <LogEntry key={logEntry.id} logEntry={logEntry} />
  })
  return (
    <div className="m-4 flex flex-col items-center">
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
          <div className="flex items-center">
            <div className="px-2 py-2 font-medium">
              <TextField
                className="w-24"
                label="Ticket"
                size="small"
                autoComplete="off"
              />
            </div>
            <div className="px-2 py-2">
              <TimeField
                className="w-20"
                label="Start"
                size="small"
                defaultValue={dayjs()}
                format="HH:mm"
              />
            </div>

            <div className="min-w-40 w-full px-2 py-2">
              <TextField
                className="w-full"
                label="Description"
                size="small"
                autoComplete="off"
              />
            </div>
            <div className="px-2 py-2">
              <Button variant="outlined">Start</Button>
            </div>
          </div>
          <Divider className="my-2" />
          {renderedLogEntries}
        </div>
      </LocalizationProvider>
    </div>

  );

}
