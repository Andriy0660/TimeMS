import {syncStatus} from "../consts/syncStatus.js";
import {Tooltip} from "@mui/material";
import DoneIcon from "@mui/icons-material/Done.js";
import SyncProblemIcon from "@mui/icons-material/SyncProblem.js";
import SyncDisabledIcon from "@mui/icons-material/SyncDisabled.js";

export default function TimeLogSyncIcon({status}) {
  return (
    <>
      {
        status === syncStatus.SYNCED && (
          <Tooltip title="Synchronized">
            <DoneIcon color="success" />
          </Tooltip>
        )
      }
      {
        status === syncStatus.PARTIAL_SYNCED && (
          <Tooltip title="Partial synchronized">
            <SyncProblemIcon color="warning" />
          </Tooltip>
        )
      }
      {
        status === syncStatus.NOT_SYNCED && (
          <Tooltip title="Not synchronized">
            <SyncDisabledIcon color="error" />
          </Tooltip>
        )
      }
    </>
  )
}