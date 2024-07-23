import {Button, CircularProgress, TextField} from "@mui/material";
import dayjs from "dayjs";
import {useState} from "react";
import dateTimeService from "../utils/dateTimeService.js";

export default function TimeLogCreateBar({onCreate}) {
  const [ticketAndDescription, setTicketAndDescription] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [isStarting, setIsStarting] = useState(false);

  const handleCreate = async () => {
    setIsCreating(true);
    const jiraIssuePattern = /^[A-Z]{2,}-\d+/;
    const match = ticketAndDescription.match(jiraIssuePattern);
    let ticket = null;
    let description = ticketAndDescription || null;
    if (match) {
      ticket = match[0];
      description = description.slice(ticket.length).trim();
    }
    try {
      await onCreate({ticket, description});
    } finally {
      setIsCreating(false);
    }
    setTicketAndDescription("");
  }

  const handleStart = async () => {
    setIsStarting(true);
    const jiraIssuePattern = /^[A-Z]{2,}-\d+/;
    const match = ticketAndDescription.match(jiraIssuePattern);
    let ticket = "";
    let description = ticketAndDescription;
    if (match) {
      ticket = match[0];
      description = description.slice(ticket.length).trim();
    }
    try {
      await onCreate({ticket, startTime: dateTimeService.getFormattedDateTime(dayjs()), description});
    } finally {
      setIsStarting(false);
    }
    setTicketAndDescription("");
  }
  return (
    <div className="flex items-center">
      <div className="w-full mx-2 my-2">
        <TextField
          className="w-full"
          label="Description"
          value={ticketAndDescription}
          onChange={(event) => setTicketAndDescription(event.target.value)}
          size="small"
          autoComplete="off"
          onKeyDown={async (e) => {
            if (e.ctrlKey && e.key === "Enter") {
              await handleStart();
            } else if (e.key === "Enter") {
              await handleCreate();
            }
          }}
        />
      </div>
      <div className="mx-2 my-2">
        <Button
          variant="outlined"
          onClick={handleCreate}
        >
          {isCreating ? <CircularProgress size={25} /> : "Create"}
        </Button>
      </div>
      <div className="mx-2 my-2">
        <Button
          variant="outlined"
          onClick={handleStart}
        >
          {isStarting ? <CircularProgress size={25} /> : "Start"}
        </Button>
      </div>
    </div>
  )
}