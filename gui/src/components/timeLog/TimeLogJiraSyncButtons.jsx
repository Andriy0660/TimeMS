import {ListItemIcon, ListItemText, MenuItem, Typography} from "@mui/material";
import SyncIcon from "@mui/icons-material/Sync.js";
import {syncStatus} from "../../consts/syncStatus.js";
import dateTimeService from "../../service/dateTimeService.js";
import KeyboardDoubleArrowRightIcon from "@mui/icons-material/KeyboardDoubleArrowRight.js";
import CallMissedOutgoingIcon from "@mui/icons-material/CallMissedOutgoing.js";
import CallMissedIcon from "@mui/icons-material/CallMissed.js";

export default function TimeLogJiraSyncButtons({timeLog, handleSyncForTicket, handleCreateWorklog, handleSyncIntoJira, handleSyncFromJira}) {
  return (
    <>
      {timeLog.ticket && timeLog.startTime && timeLog.endTime && timeLog.jiraSyncInfo.status !== syncStatus.NOT_SYNCED && (
        <MenuItem onClick={() => handleSyncForTicket(timeLog.ticket)}>
          <ListItemIcon>
            <SyncIcon color="primary" fontSize="small" />
          </ListItemIcon>
          <ListItemText>
            <Typography className="text-sm">Synchronize</Typography>
          </ListItemText>
        </MenuItem>
      )
      }

      {
        (timeLog.ticket && timeLog.startTime && timeLog.endTime && timeLog.jiraSyncInfo.status === syncStatus.NOT_SYNCED) && (
          <MenuItem onClick={() => handleCreateWorklog({
            ticket: timeLog.ticket,
            date: dateTimeService.getFormattedDate(timeLog.date),
            startTime: dateTimeService.getFormattedDateTime(timeLog.startTime),
            endTime: dateTimeService.getFormattedDateTime(timeLog.endTime),
            description: timeLog.description
          })}>
            <ListItemIcon>
              <KeyboardDoubleArrowRightIcon color="primary" fontSize="small" />
            </ListItemIcon>
            <ListItemText>
              <Typography className="text-sm">Save to worklogs</Typography>
            </ListItemText>
          </MenuItem>
        )
      }

      {
        (timeLog.ticket && timeLog.startTime && timeLog.endTime && timeLog.jiraSyncInfo.status === syncStatus.PARTIAL_SYNCED) && (
          [
            <MenuItem
              key="to"
              onClick={() => handleSyncIntoJira({
                ticket: timeLog.ticket,
                date: dateTimeService.getFormattedDate(timeLog.date),
                description: timeLog.description,
              })}
            >
              <ListItemIcon>
                <CallMissedOutgoingIcon color="primary" fontSize="small" />
              </ListItemIcon>
              <ListItemText>
                <Typography className="text-sm">Sync to jira</Typography>
              </ListItemText>
            </MenuItem>,
            <MenuItem
              key="from"
              onClick={() => handleSyncFromJira({
                ticket: timeLog.ticket,
                date: dateTimeService.getFormattedDate(timeLog.date),
                description: timeLog.description,
              })}
            >
              <ListItemIcon>
                <CallMissedIcon color="primary" fontSize="small" />
              </ListItemIcon>
              <ListItemText>
                <Typography className="text-sm">Sync from jira</Typography>
              </ListItemText>
            </MenuItem>
          ]
        )
      }
    </>
  )
}