import {syncStatus} from "../../consts/syncStatus.js";
import {Tooltip} from "@mui/material";
import DoneIcon from "@mui/icons-material/Done.js";
import SyncDisabledIcon from "@mui/icons-material/SyncDisabled.js";

export default function TimeLogUpworkSyncStatusIcon({showOnlyNotSuccessfullySynced, status, className}) {
  return (
    <div className={`flex items-center ${className}`}>
      {!showOnlyNotSuccessfullySynced && status === syncStatus.SYNCED && (
        <Tooltip title="Upwork: Synchronized">
          <DoneIcon color="success" />
        </Tooltip>
      )
      }
      {
        status === syncStatus.NOT_SYNCED && (
          <Tooltip title="Upwork: Not synchronized">
            <SyncDisabledIcon color="error" />
          </Tooltip>
        )
      }
    </div>
  )
}