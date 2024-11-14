import {syncStatus} from "../../consts/syncStatus.js";
import {Tooltip} from "@mui/material";
import DoneIcon from "@mui/icons-material/Done.js";
import SyncProblemIcon from "@mui/icons-material/SyncProblem.js";
import SyncDisabledIcon from "@mui/icons-material/SyncDisabled.js";

export default function TimeLogSyncStatusIcon({showOnlyNotSuccessfullySynced, status, serviceName, className}) {
  return (
    <div className={`flex items-center ${className}`}>
      {!showOnlyNotSuccessfullySynced && status === syncStatus.SYNCED && (
          <Tooltip title={`${serviceName}: Synchronized`}>
            <DoneIcon color="success" />
          </Tooltip>
        )
      }
      {
        status === syncStatus.PARTIAL_SYNCED && (
          <Tooltip title={`${serviceName}: Partial synchronized`}>
            <SyncProblemIcon color="warning" />
          </Tooltip>
        )
      }
      {
        status === syncStatus.NOT_SYNCED && (
          <Tooltip title={`${serviceName}: Not synchronized`}>
            <SyncDisabledIcon color="error" />
          </Tooltip>
        )
      }
    </div>
  )
}