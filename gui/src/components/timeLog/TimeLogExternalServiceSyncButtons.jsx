import {syncStatus} from "../../consts/syncStatus.js";
import {ListItemIcon, ListItemText, MenuItem, Typography} from "@mui/material";
import dateTimeService from "../../service/dateTimeService.js";
import KeyboardDoubleArrowRightIcon from "@mui/icons-material/KeyboardDoubleArrowRight.js";
import CallMissedOutgoingIcon from "@mui/icons-material/CallMissedOutgoing.js";

export default function TimeLogExternalServiceSyncButtons({timeLog, handleCreateExternalTimeLog, handleSyncIntoExternalService}) {
  return (
    <>
      {
        (timeLog.startTime && timeLog.endTime && timeLog.externalTimeLogSyncInfo.status === syncStatus.NOT_SYNCED) && (
          <MenuItem onClick={() => handleCreateExternalTimeLog({
            date: dateTimeService.getFormattedDate(timeLog.date),
            startTime: dateTimeService.getFormattedDateTime(timeLog.startTime),
            endTime: dateTimeService.getFormattedDateTime(timeLog.endTime),
            description: timeLog.description
          })}>
            <ListItemIcon>
              <KeyboardDoubleArrowRightIcon color="primary" fontSize="small" />
            </ListItemIcon>
            <ListItemText>
              <Typography className="text-sm">Save to external time logs</Typography>
            </ListItemText>
          </MenuItem>
        )
      }

      {
        (timeLog.startTime && timeLog.endTime && timeLog.externalTimeLogSyncInfo.status === syncStatus.PARTIAL_SYNCED) && (
          <MenuItem
            onClick={() => handleSyncIntoExternalService({
              date: dateTimeService.getFormattedDate(timeLog.date),
              description: timeLog.description
            })}
          >
            <ListItemIcon>
              <CallMissedOutgoingIcon color="primary" fontSize="small" />
            </ListItemIcon>
            <ListItemText>
              <Typography className="text-sm">Sync to external service</Typography>
            </ListItemText>
          </MenuItem>
        )
      }
    </>
  )
}