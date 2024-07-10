import {Button, TextField} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useState} from "react";

export default function CreateLogEntry() {
  const [ticket, setTicket] = useState("");
  const [startTime, setStartTime] = useState(dayjs());
  const [description, setDescription] = useState("");
  return(
    <div className="flex items-center">
      <div className="px-2 py-2 font-medium">
        <TextField
          className="w-24"
          label="Ticket"
          value={ticket}
          onChange={(event) => setTicket(event.target.value)}
          size="small"
          autoComplete="off"
        />
      </div>
      <div className="px-2 py-2">
        <TimeField
          error={!startTime}
          className="w-20"
          label="Start"
          size="small"
          value={startTime}
          onChange={(date) => {
            setStartTime(!dayjs(date).isValid() ? null : dayjs(date))
          }}
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
        <Button
          variant="outlined"
          disabled={!startTime}
        >Start</Button>
      </div>
    </div>
  )
}