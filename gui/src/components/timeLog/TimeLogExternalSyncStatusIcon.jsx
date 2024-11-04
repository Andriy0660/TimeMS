import {syncStatus} from "../../consts/syncStatus.js";
import {Tooltip} from "@mui/material";
import DoneIcon from "@mui/icons-material/Done.js";
import SyncDisabledIcon from "@mui/icons-material/SyncDisabled.js";
import SyncProblemIcon from "@mui/icons-material/SyncProblem.js";

export default function TimeLogExternalSyncStatusIcon({showOnlyNotSuccessfullySynced, status, className}) {
  return (
    <div className={`flex items-center ${className}`}>
      {!showOnlyNotSuccessfullySynced && status === syncStatus.SYNCED && (
        <Tooltip title="External Service: Synchronized">
          <DoneIcon color="success" />
        </Tooltip>
      )
      }
      {
        status === syncStatus.PARTIAL_SYNCED && (
          <Tooltip title="External Service: Partial synchronized">
            <SyncProblemIcon color="warning" />
          </Tooltip>
        )
      }
      {
        status === syncStatus.NOT_SYNCED && (
          <Tooltip title="External Service: Not synchronized">
            <SyncDisabledIcon color="error" />
          </Tooltip>
        )
      }
    </div>
  )
}