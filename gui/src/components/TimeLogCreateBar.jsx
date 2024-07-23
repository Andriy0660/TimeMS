import {Button, CircularProgress, IconButton, TextField} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useState} from "react";
import RestoreIcon from '@mui/icons-material/Restore';
import dateTimeService from "../utils/dateTimeService.js";

export default function TimeLogCreateBar({onCreate}) {
  const [startTime, setStartTime] = useState(dayjs());
  const [ticketAndDescription, setTicketAndDescription] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const handleCreate = async () => {
    setIsCreating(true);
    const jiraIssuePattern = /^[A-Z]{2,}-\d+/;
    const match = ticketAndDescription.match(jiraIssuePattern);
    let ticket = "";
    let description = ticketAndDescription;
    if (match) {
      ticket = match[0];
      description = description.slice(ticket.length).trim();
    }
    try {
      await onCreate({ticket, startTime: dateTimeService.getFormattedDateTime(startTime), description});
    } finally {
      setIsCreating(false);
    }
    setTicketAndDescription("");
  }
  return (
    <div className="flex items-center">
      <div className="ml-2 my-2">
        <TimeField
          className="w-20"
          label="Start"
          size="small"
          value={startTime}
          onChange={(date) => {
            if (date === null) {
              setStartTime(null);
            } else if (dayjs(date).isValid()) {
              setStartTime(dayjs(date))
            }
          }}
          format="HH:mm"
        />
      </div>
      <IconButton size="small" onClick={() => setStartTime(dayjs())}><RestoreIcon fontSize="small" /></IconButton>

      <div className="min-w-40 w-full mx-2 my-2">
        <TextField
          className="w-full"
          label="Description"
          value={ticketAndDescription}
          onChange={(event) => setTicketAndDescription(event.target.value)}
          size="small"
          autoComplete="off"
          onKeyDown={async (e) => {
            if (e.key === "Enter") {
              if (startTime) {
                await handleCreate();
              }
            }
          }}
        />
      </div>
      <div className="mx-2 my-2">
        <Button
          variant="outlined"
          onClick={handleCreate}
        >
          {isCreating ? <CircularProgress size={25} /> : "Start"}
        </Button>
      </div>
    </div>
  )
}